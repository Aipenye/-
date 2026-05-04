package com.wms.agv.engine;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 仓库栅格地图：80m × 40m，精度 0.5m/格，共 160×80 格
 *
 * 坐标系：x 向右（东），y 向上（北）
 * 格子 (col, row) 对应真实坐标 (col*0.5, row*0.5)
 */
@Component
public class WarehouseMap {

    public static final double WIDTH_M  = 80.0;
    public static final double HEIGHT_M = 40.0;
    public static final double CELL_SIZE = 0.5;
    public static final int COLS = (int)(WIDTH_M  / CELL_SIZE); // 160
    public static final int ROWS = (int)(HEIGHT_M / CELL_SIZE); // 80

    // true = AGV 不可通行
    private final boolean[][] obstacle = new boolean[COLS][ROWS];
    // 代价权重（1.0 正常，2.0 靠近障碍，障碍本身用 obstacle 标记）
    private final double[][] cost = new double[COLS][ROWS];

    // 出口区域 [x1,y1,x2,y2]（用于前端渲染）
    public static final double EXIT_X1 = 37.0;
    public static final double EXIT_X2 = 43.0;
    public static final double EXIT_Y  = 0.0;   // 出口在 y=0 下墙

    // 货架矩形列表（用于前端渲染）[x1,y1,x2,y2] 单位：米
    private final List<double[]> shelfRects = new ArrayList<>();
    // 人工通道禁区（AGV 禁止进入）[x1,y1,x2,y2]
    private final List<double[]> humanZones = new ArrayList<>();

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
        // ── 人工通道（靠墙，AGV 禁区）──────────────────────────
        addHumanZone(0,    0,    1.5,  40);   // 左墙
        addHumanZone(78.5, 0,    80,   40);   // 右墙
        addHumanZone(0,    38.5, 80,   40);   // 上墙
        addHumanZone(0,    0,    80,   1.5);  // 下墙

        // ── 货架布局 ─────────────────────────────────────────
        // 主干道：水平 y∈[18.25,21.75]，垂直 x∈[38.25,41.75]
        // 左半区货架列（x 方向）：A[2,10]  B[13,21]  C[24,32]
        // 右半区货架列：          D[48,56]  E[59,67]  F[70,78]
        // 下半区货架行（y 方向）：Row1[2,4]  Row2[6.5,8.5]  Row3[11,13]  Row4[15.5,17.5]
        // 上半区货架行：          Row5[22.5,24.5] Row6[27,29] Row7[31.5,33.5] Row8[36,38]

        double[][] xRanges = {{2,10},{13,21},{24,32},{48,56},{59,67},{70,78}};
        double[][] yRanges = {{2,4},{6.5,8.5},{11,13},{15.5,17.5},
                              {22.5,24.5},{27,29},{31.5,33.5},{36,38}};

        for (double[] xr : xRanges) {
            for (double[] yr : yRanges) {
                addShelf(xr[0], yr[0], xr[1], yr[1]);
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
        // 障碍物周围 1 格（0.5m）提高代价，引导路径远离边缘
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
}
