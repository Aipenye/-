package com.wms.agv.engine;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * A* 路径规划，在 WarehouseMap 栅格上运行
 * 返回路径点列表（米坐标），已做简单折线平滑
 */
@Component
public class AStarPathfinder {

    private final WarehouseMap map;

    public AStarPathfinder(WarehouseMap map) {
        this.map = map;
    }

    public List<double[]> findPath(double startX, double startY, double goalX, double goalY) {
        return findPath(startX, startY, goalX, goalY, null);
    }

    public List<double[]> findPath(double startX, double startY, double goalX, double goalY,
                                   java.util.Set<Integer> tempObstacles) {
        int sc = map.meterToCol(startX), sr = map.meterToRow(startY);
        int gc = map.meterToCol(goalX),  gr = map.meterToRow(goalY);

        // 起点在障碍内时，移到最近的空闲格
        if (isBlocked(sc, sr, tempObstacles)) {
            int[] nearest = findNearestFree(sc, sr, tempObstacles);
            if (nearest == null) return Collections.emptyList();
            sc = nearest[0]; sr = nearest[1];
        }

        // 目标在障碍内时，移到最近的空闲格
        if (isBlocked(gc, gr, tempObstacles)) {
            int[] nearest = findNearestFree(gc, gr, tempObstacles);
            if (nearest == null) return Collections.emptyList();
            gc = nearest[0]; gr = nearest[1];
        }

        if (sc == gc && sr == gr) return List.of(new double[]{map.colToMeter(gc), map.rowToMeter(gr)});

        int total = WarehouseMap.COLS * WarehouseMap.ROWS;
        double[] g = new double[total];
        double[] f = new double[total];
        int[]    parent = new int[total];
        Arrays.fill(g, Double.MAX_VALUE);
        Arrays.fill(parent, -1);

        PriorityQueue<int[]> open = new PriorityQueue<>(Comparator.comparingDouble(a -> f[idx(a[0], a[1])]));

        int startIdx = idx(sc, sr);
        g[startIdx] = 0;
        f[startIdx] = heuristic(sc, sr, gc, gr);
        open.offer(new int[]{sc, sr});

        boolean[] closed = new boolean[total];

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

        while (!open.isEmpty()) {
            int[] cur = open.poll();
            int cc = cur[0], cr = cur[1];
            int ci = idx(cc, cr);
            if (closed[ci]) continue;
            closed[ci] = true;

            if (cc == gc && cr == gr) {
                return reconstructPath(parent, gc, gr,
                        map.colToMeter(gc), map.rowToMeter(gr));
            }

            for (int[] d : dirs) {
                int nc = cc + d[0], nr = cr + d[1];
                if (isBlocked(nc, nr, tempObstacles)) continue;
                int ni = idx(nc, nr);
                if (closed[ni]) continue;

                double moveCost = (d[0] != 0 && d[1] != 0) ? 1.414 : 1.0;
                double ng = g[ci] + moveCost * map.getCost(nc, nr);
                if (ng < g[ni]) {
                    g[ni] = ng;
                    f[ni] = ng + heuristic(nc, nr, gc, gr);
                    parent[ni] = ci;
                    open.offer(new int[]{nc, nr});
                }
            }
        }
        return Collections.emptyList();
    }

    private List<double[]> reconstructPath(int[] parent, int gc, int gr, double goalX, double goalY) {
        List<int[]> cells = new ArrayList<>();
        int cur = idx(gc, gr);
        while (cur != -1) {
            int c = cur % WarehouseMap.COLS;
            int r = cur / WarehouseMap.COLS;
            cells.add(new int[]{c, r});
            cur = parent[cur];
        }
        Collections.reverse(cells);

        // 转换为米坐标并平滑
        List<double[]> path = new ArrayList<>();
        for (int[] cell : cells) {
            path.add(new double[]{map.colToMeter(cell[0]), map.rowToMeter(cell[1])});
        }
        // 最后一个点精确到目标
        if (!path.isEmpty()) {
            path.set(path.size() - 1, new double[]{goalX, goalY});
        }
        return smoothPath(path);
    }

    /** 移除共线中间点，减少不必要的转向 */
    private List<double[]> smoothPath(List<double[]> path) {
        if (path.size() <= 2) return path;
        List<double[]> smooth = new ArrayList<>();
        smooth.add(path.get(0));
        for (int i = 1; i < path.size() - 1; i++) {
            double[] prev = path.get(i - 1);
            double[] cur  = path.get(i);
            double[] next = path.get(i + 1);
            double cross = (cur[0]-prev[0])*(next[1]-prev[1]) - (cur[1]-prev[1])*(next[0]-prev[0]);
            if (Math.abs(cross) > 0.01) smooth.add(cur);
        }
        smooth.add(path.get(path.size() - 1));
        return smooth;
    }

    private int[] findNearestFree(int gc, int gr) {
        return findNearestFree(gc, gr, null);
    }

    private int[] findNearestFree(int gc, int gr, java.util.Set<Integer> tempObstacles) {
        for (int r = 1; r <= 10; r++) {
            for (int dc = -r; dc <= r; dc++) {
                for (int dr = -r; dr <= r; dr++) {
                    if (Math.abs(dc) != r && Math.abs(dr) != r) continue;
                    int nc = gc + dc, nr = gr + dr;
                    if (!isBlocked(nc, nr, tempObstacles)) return new int[]{nc, nr};
                }
            }
        }
        return null;
    }

    private boolean isBlocked(int col, int row, java.util.Set<Integer> tempObstacles) {
        if (map.isObstacle(col, row)) return true;
        if (tempObstacles == null || tempObstacles.isEmpty()) return false;
        return tempObstacles.contains(col * 1000 + row);
    }

    private double heuristic(int c1, int r1, int c2, int r2) {
        return Math.sqrt((c1-c2)*(double)(c1-c2) + (r1-r2)*(double)(r1-r2));
    }

    private int idx(int col, int row) {
        return row * WarehouseMap.COLS + col;
    }
}
