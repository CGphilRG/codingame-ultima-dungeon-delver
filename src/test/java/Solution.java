import java.util.*;

/**
 * Orienteering / Resource-Constrained Shortest-Path Solver for Ultima Dungeon Delver:
 * - Computes exact BFS distances on the dungeon graph
 * - Greedily visits high-value Chests/Relics while ensuring enough Torch budget remains to reach Exit 'E'
 * - Uses FLAM_POR to clear blocking Monsters/Relic Guardians and MANI when HP is low
 */
public class Solution {
    static final int[] DX = {0, 1, 0, -1};
    static final int[] DY = {-1, 0, 1, 0};

    static int[][] bfs(String[] grid, int w, int h, int sx, int sy) {
        int[][] dist = new int[h][w];
        for (int y = 0; y < h; y++) Arrays.fill(dist[y], 999);
        Queue<int[]> q = new ArrayDeque<>();
        dist[sy][sx] = 0;
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int cx = cur[0], cy = cur[1];
            for (int d = 0; d < 4; d++) {
                int nx = cx + DX[d], ny = cy + DY[d];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && grid[ny].charAt(nx) != '#' && dist[ny][nx] > dist[cy][cx] + 1) {
                    dist[ny][nx] = dist[cy][cx] + 1;
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return dist;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();

        while (true) {
            int ax = in.nextInt(), ay = in.nextInt();
            int hp = in.nextInt(), mp = in.nextInt(), torch = in.nextInt(), gold = in.nextInt();
            String[] grid = new String[height];
            for (int y = 0; y < height; y++) grid[y] = in.next();

            int count = in.nextInt();
            List<int[]> entities = new ArrayList<>();
            int exitX = width - 2, exitY = height - 2;
            for (int i = 0; i < count; i++) {
                int id = in.nextInt();
                char type = in.next().charAt(0);
                int x = in.nextInt(), y = in.nextInt(), val = in.nextInt(), ehp = in.nextInt();
                entities.add(new int[]{id, type, x, y, val, ehp});
                if (type == 'E') {
                    exitX = x;
                    exitY = y;
                }
            }

            int[][] distFromAvatar = bfs(grid, width, height, ax, ay);
            int[][] distToExit = bfs(grid, width, height, exitX, exitY);

            int targetX = exitX, targetY = exitY;
            double bestScore = -1e9;

            for (int[] e : entities) {
                char type = (char) e[1];
                if (type == 'E') continue;
                int ex = e[2], ey = e[3], val = e[4];
                int d1 = distFromAvatar[ey][ex];
                int d2 = distToExit[ey][ex];
                if (d1 + d2 + 2 <= torch) {
                    double score = (type == 'F' && (hp < 25 || torch < 25)) ? 80.0 / Math.max(1, d1)
                                 : (val * 10.0) / Math.max(1, d1);
                    if (score > bestScore) {
                        bestScore = score;
                        targetX = ex;
                        targetY = ey;
                    }
                }
            }

            String spell = "";
            if (hp <= 22 && mp >= 4) {
                spell = " MANI";
            } else {
                for (int[] e : entities) {
                    if (e[5] > 0) {
                        int d = Math.abs(ax - e[2]) + Math.abs(ay - e[3]);
                        if (d <= 3 && mp >= 5) {
                            spell = " FLAM_POR " + e[2] + " " + e[3];
                            break;
                        } else if (d <= 1) {
                            spell = " STRIKE " + e[2] + " " + e[3];
                            break;
                        }
                    }
                }
            }

            System.out.println("MOVE " + targetX + " " + targetY + spell);
        }
    }
}
