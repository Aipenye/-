package com.wms.agv.engine;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 仓库栅格地图：80m × 40m，精度 0.5m/格，共 160×80 格
 *
 * 坐标系：x 向右（东），y 向上（北）
 * 格子 (col, row) 对应真实坐标 (col*0.5, row*0.5)
 *
 * 货架编号规则（1-48）：
 *   6列(A-F) × 8行(Row1-8) = 48个货架格子
 *   编号 = (列索引)*8 + (行索引+1)，列从左到右，行从下到上
 *   A列: 1-8, B列: 9-16, C列: 17-24, D列: 25-32, E列: 33-40, F列: 41-48
 */
@Component
public class WarehouseMap {

    public static final double WIDTH_M   = 80.0;
    public static final double HEIGHT_M  = 40.0;
    public static final double CELL_SIZE = 0.5;
    public static final int COLS = (int)(WIDTH_M  / CELL_SIZE); // 160
    public static final int ROWS = (int)(HEIGHT_M / CELL_SIZE); // 80

    // 出口区域：x∈[37.5,42.5]（居中于80m宽仓库），y=0（下墙）
    public static final double EXIT_X1 = 37.5;
    public static final double EXIT_X2 = 42.5;
    public static final double EXIT_Y  = 0.0;

    // 左侧停车区：x∈[32.5,37.5]（距C列货架右边缘0.5m），y∈[1.5,4]
    public static final double LEFT_PARK_X1 = 32.5;
    public static final double LEFT_PARK_X2 = 37.5;
    // 右侧停车区：x∈[42.5,47.5]（距D列货架左边缘0.5m），y∈[1.5,4]
    public static final double RIGHT_PARK_X1 = 42.5;
    public static final double RIGHT_PARK_X2 = 47.5;
    public static final double PARK_Y1 = 1.5;
    public static final double PARK_Y2 = 4.0;

    // 四个独立停车位：左区两辆，右区两辆
    public static final double[][] PARK_SLOTS = {
        {33.5, 2.5},
        {36.5, 2.5},
        {43.5, 2.5},
        {46.5, 2.5}
    };

    // 每个停车位正北方向的出入口点（y=5.5，位于停车区外）
    public static final double[][] PARK_EXIT_POINTS = {
        {33.5, 5.5},
        {36.5, 5.5},
        {43.5, 5.5},
        {46.5, 5.5}
    };

    private final boolean[][] obstacle = new boolean[COLS][ROWS];
    private final double[][]  cost     = new double[COLS][ROWS];

    private final List<double[]> shelfRects  = new ArrayList<>();
    private final List<double[]> humanZones  = new ArrayList<>();

    // 货架编号 → 中心坐标 [x, y]
    private final Map<Integer, double[]> slotCenterMap = new LinkedHashMap<>();
    // 货架编号 → 货架矩形 [x1,y1,x2,y2]
    private final Map<Integer, double[]> slotRectMap   = new LinkedHashMap<>();
    // 已注册的货架编号集合（由 StorageService 动态维护）
    private final Set<Integer> registeredSlots = Collections.synchronizedSet(new HashSet<>());

    public WarehouseMap() {
        initCosts();
        buildLayout();
        buildCostmap();
    }

    private void initCosts() {
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                cost[c][r] = 1.0;
    }

    private void buildLayout() {
        // 人工通道（靠墙，AGV禁区）
        addHumanZone(0,    0,    1.5,  40);
        addHumanZone(78.5, 0,    80,   40);
        addHumanZone(0,    38.5, 80,   40);
        addHumanZone(0,    0,    80,   1.5);

        // 货架列 x 范围
        double[][] xRanges = {{2,10},{13,21},{24,32},{48,56},{59,67},{70,78}};
        // 货架行 y 范围
        double[][] yRanges = {{2,4},{6.5,8.5},{11,13},{15.5,17.5},
                              {22.5,24.5},{27,29},{31.5,33.5},{36,38}};

        String[] colNames = {"A","B","C","D","E","F"};
        for (int ci = 0; ci < xRanges.length; ci++) {
            for (int ri = 0; ri < yRanges.length; ri++) {
                int slot = ci * 8 + (ri + 1); // 1-48
                double x1 = xRanges[ci][0], x2 = xRanges[ci][1];
                double y1 = yRanges[ri][0], y2 = yRanges[ri][1];
                double cx = (x1 + x2) / 2.0;
                double cy = (y1 + y2) / 2.0;
                slotCenterMap.put(slot, new double[]{cx, cy});
                slotRectMap.put(slot, new double[]{x1, y1, x2, y2});
                // 货架本身始终是障碍（实体存在）
                addShelf(x1, y1, x2, y2);
            }
        }
    }

    private void addHumanZone(double x1, double y1, double x2, double y2) {
        humanZones.add(new double[]{x1, y1, x2, y2});
        markObstacle(x1, y1, x2, y2);
    }

    private void addShelf(double x1, double y1, double x2, double y2) {
        shelfRects.add(new double[]{x1, y1, x2, y2});
        markObstacle(x1, y1, x2, y2);
    }

    private void markObstacle(double x1, double y1, double x2, double y2) {
        int c1 = meterToCol(x1), c2 = meterToCol(x2);
        int r1 = meterToRow(y1), r2 = meterToRow(y2);
        for (int c = c1; c < c2 && c < COLS; c++)
            for (int r = r1; r < r2 && r < ROWS; r++)
                obstacle[c][r] = true;
    }

    private void buildCostmap() {
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                if (obstacle[c][r]) {
                    cost[c][r] = Double.MAX_VALUE;
                    for (int dc = -2; dc <= 2; dc++) {
                        for (int dr = -2; dr <= 2; dr++) {
                            int nc = c + dc, nr = r + dr;
                            if (nc >= 0 && nc < COLS && nr >= 0 && nr < ROWS && !obstacle[nc][nr]) {
                                cost[nc][nr] = Math.max(cost[nc][nr], 2.0);
                            }
                        }
                    }
                }
            }
        }
    }

    // ── 注册管理 ──────────────────────────────────────────────

    /** 注册货架编号（仓库创建时调用） */
    public void registerSlot(int slot) {
        registeredSlots.add(slot);
    }

    /** 注销货架编号（仓库删除时调用） */
    public void unregisterSlot(int slot) {
        registeredSlots.remove(slot);
    }

    /** 批量同步注册状态 */
    public void syncRegisteredSlots(Collection<Integer> slots) {
        registeredSlots.clear();
        if (slots != null) registeredSlots.addAll(slots);
    }

    public boolean isRegistered(int slot) {
        return registeredSlots.contains(slot);
    }

    /**
     * 找到距出口最近且未注册的货架编号。
     * 出口中心 (37.5, 0)，按欧氏距离排序。
     */
    public int findNearestUnregisteredSlot() {
        double exitX = (EXIT_X1 + EXIT_X2) / 2.0;
        double exitY = EXIT_Y;
        return slotCenterMap.entrySet().stream()
                .filter(e -> !registeredSlots.contains(e.getKey()))
                .min(Comparator.comparingDouble(e -> {
                    double[] c = e.getValue();
                    return Math.sqrt(Math.pow(c[0]-exitX,2) + Math.pow(c[1]-exitY,2));
                }))
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    /**
     * 获取货架编号对应的AGV停靠坐标。
     * 返回货架正下方通道中心（y = shelf_y1 - 1.0），保证目标点在可通行区域内。
     * 若下方空间不足（靠近底墙），则取上方通道。
     */
    public double[] getSlotApproachPoint(int slot) {
        double[] rect = slotRectMap.get(slot);
        if (rect == null) return null;
        double cx = (rect[0] + rect[2]) / 2.0;
        double y1 = rect[1]; // 货架下边缘
        double y2 = rect[3]; // 货架上边缘
        // 优先停在货架下方通道（y1 - 1.0），若低于人工通道禁区(1.5m)则改用上方
        double approachY = (y1 - 1.0 >= 2.0) ? y1 - 1.0 : y2 + 1.0;
        return new double[]{cx, approachY};
    }

    // ── 查询接口 ──────────────────────────────────────────────

    public boolean isObstacle(int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return true;
        return obstacle[col][row];
    }

    public double getCost(int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return Double.MAX_VALUE;
        return cost[col][row];
    }

    public int meterToCol(double x) { return (int)(x / CELL_SIZE); }
    public int meterToRow(double y) { return (int)(y / CELL_SIZE); }
    public double colToMeter(int col) { return col * CELL_SIZE + CELL_SIZE / 2.0; }
    public double rowToMeter(int row) { return row * CELL_SIZE + CELL_SIZE / 2.0; }

    public List<double[]> getShelfRects()  { return shelfRects; }
    public List<double[]> getHumanZones()  { return humanZones; }
    public Map<Integer, double[]> getSlotCenterMap() { return slotCenterMap; }
    public Map<Integer, double[]> getSlotRectMap()   { return slotRectMap; }
    public Set<Integer> getRegisteredSlots()         { return Collections.unmodifiableSet(registeredSlots); }
}
