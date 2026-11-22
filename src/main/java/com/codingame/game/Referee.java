package com.codingame.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Polygon;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.toggle.ToggleModule;
import com.codingame.gameengine.module.tooltip.TooltipModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    public static final int WIDTH = 13;
    public static final int HEIGHT = 11;
    public static final int MAX_TURNS = 70;
    private static final int CELL_SIZE = 74;
    private static final int ORIGIN_X = (1920 - WIDTH * CELL_SIZE) / 2;
    private static final int ORIGIN_Y = (1080 - HEIGHT * CELL_SIZE) / 2 + 25;

    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};

    public static class DungeonItem {
        int id;
        char type; // 'C'=Chest(25g), 'R'=Relic(60g), 'M'=Monster(15hp, 20g), 'F'=Fountain(+18hp,+10torch), 'E'=Exit
        int x, y;
        int value;
        int hp;
        boolean active = true;
        Group group;
        Text debugText;
    }

    @Inject private SoloGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private TooltipModule tooltipModule;
    @Inject private ToggleModule toggleModule;

    private final char[][] grid = new char[HEIGHT][WIDTH];
    private final List<DungeonItem> items = new ArrayList<>();
    private int avatarX = 1;
    private int avatarY = 5;
    private int hp = 40;
    private final int maxHp = 40;
    private int mp = 20;
    private int torch = 65;
    private int gold = 0;

    // Avatar Graphics
    private Group avatarGroup;
    private Circle torchHalo;
    private Rectangle hpBarFill;
    private Text avatarDebugText;
    private Text hudStatsText;
    private Text turnText;
    private Sprite fxSpell;

    private int cellX(int x) {
        return ORIGIN_X + x * CELL_SIZE + CELL_SIZE / 2;
    }

    private int cellY(int y) {
        return ORIGIN_Y + y * CELL_SIZE + CELL_SIZE / 2;
    }

    @Override
    public void init() {
        gameManager.setMaxTurns(MAX_TURNS);
        gameManager.setFirstTurnMaxTime(1000);
        gameManager.setTurnMaxTime(50);
        gameManager.setFrameDuration(450);

        List<String> testLines = gameManager.getTestCaseInput();
        parseDungeonMap(testLines);
        initGraphics();

        Player player = gameManager.getPlayer();
        player.sendInputLine(String.format("%d %d", WIDTH, HEIGHT));
    }

    private void parseDungeonMap(List<String> lines) {
        int itemId = 0;
        for (int y = 0; y < HEIGHT; y++) {
            String row = (y < lines.size()) ? lines.get(y).trim() : "#############";
            for (int x = 0; x < WIDTH; x++) {
                char c = (x < row.length()) ? row.charAt(x) : '#';
                if (c == '#') {
                    grid[y][x] = '#';
                } else {
                    grid[y][x] = '.';
                    if (c == 'S') {
                        avatarX = x;
                        avatarY = y;
                    } else if (c == 'C' || c == 'R' || c == 'M' || c == 'F' || c == 'E') {
                        DungeonItem item = new DungeonItem();
                        item.id = itemId++;
                        item.type = c;
                        item.x = x;
                        item.y = y;
                        item.value = (c == 'C') ? 25 : (c == 'R' ? 60 : (c == 'M' ? 20 : (c == 'E' ? 50 : 0)));
                        item.hp = (c == 'M') ? 15 : (c == 'R' ? 15 : 0);
                        items.add(item);
                    }
                }
            }
        }
    }

    private void initGraphics() {
        graphicEntityModule.createSprite()
            .setImage("background.jpg")
            .setBaseWidth(1920)
            .setBaseHeight(1080)
            .setZIndex(-20);

        graphicEntityModule.createRectangle()
            .setX(ORIGIN_X - 14)
            .setY(ORIGIN_Y - 14)
            .setWidth(WIDTH * CELL_SIZE + 28)
            .setHeight(HEIGHT * CELL_SIZE + 28)
            .setFillColor(0x090d16)
            .setAlpha(0.90)
            .setLineWidth(4)
            .setLineColor(0x4a5568)
            .setZIndex(-15);

        graphicEntityModule.createText("ULTIMA DUNGEON DELVER")
            .setX(960)
            .setY(38)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(42)
            .setFillColor(0xf6e05e)
            .setStrokeThickness(4)
            .setStrokeColor(0x000000)
            .setZIndex(30);

        turnText = graphicEntityModule.createText("TURN 0 / " + MAX_TURNS)
            .setX(320)
            .setY(92)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(36)
            .setFillColor(0xa0aec0)
            .setZIndex(30);

        hudStatsText = graphicEntityModule.createText("HP: 40 | MP: 20 | TORCH: 65 | GOLD: 0")
            .setX(1140)
            .setY(92)
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(36)
            .setFillColor(0xffffff)
            .setZIndex(30);

        // Dungeon Floor & Walls (Dual-Mode)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int cx = cellX(x), cy = cellY(y);
                boolean isWall = grid[y][x] == '#';

                String img = isWall ? "pillar.png" : (((x + y) % 2 == 0) ? "floor1.png" : "floor2.png");
                Sprite ts = graphicEntityModule.createSprite()
                    .setImage(img)
                    .setX(cx)
                    .setY(cy)
                    .setAnchor(0.5)
                    .setBaseWidth(CELL_SIZE - 2)
                    .setBaseHeight(CELL_SIZE - 2)
                    .setZIndex(1);
                toggleModule.displayOnToggleState(ts, "renderMode", true);

                Rectangle dt = graphicEntityModule.createRectangle()
                    .setX(cx - CELL_SIZE / 2 + 2)
                    .setY(cy - CELL_SIZE / 2 + 2)
                    .setWidth(CELL_SIZE - 4)
                    .setHeight(CELL_SIZE - 4)
                    .setFillColor(isWall ? 0x3b4252 : 0x141a24)
                    .setLineWidth(2)
                    .setLineColor(isWall ? 0x81a1c1 : 0x2d3748)
                    .setZIndex(1);
                toggleModule.displayOnToggleState(dt, "renderMode", false);
            }
        }

        // Items (Dual-Mode: RPG Sprites vs Geometric Debug Shapes)
        for (DungeonItem item : items) {
            String spriteImg = "chest_closed.png";
            int debugColor = 0xf6e05e;
            if (item.type == 'M') {
                spriteImg = "monster_skeleton.png";
                debugColor = 0xf56565;
            } else if (item.type == 'R') {
                spriteImg = "torch.png";
                debugColor = 0xd69e2e;
            } else if (item.type == 'F') {
                spriteImg = "fx_heal.png";
                debugColor = 0x4fd1c5;
            } else if (item.type == 'E') {
                spriteImg = "stairs.png";
                debugColor = 0x68d391;
            }

            Sprite sp = graphicEntityModule.createSprite()
                .setImage(spriteImg)
                .setAnchor(0.5)
                .setBaseWidth(54)
                .setBaseHeight(54);
            toggleModule.displayOnToggleState(sp, "renderMode", true);

            Polygon geo = graphicEntityModule.createPolygon()
                .addPoint(0, -24)
                .addPoint(24, 0)
                .addPoint(0, 24)
                .addPoint(-24, 0)
                .setFillColor(debugColor)
                .setLineWidth(3)
                .setLineColor(0xffffff);
            Text dl = graphicEntityModule.createText(String.valueOf(item.type))
                .setAnchor(0.5)
                .setFontFamily("Lato")
                .setFontSize(36)
                .setFillColor(0x000000);
            item.debugText = dl;
            Group dbg = graphicEntityModule.createGroup(geo, dl);
            toggleModule.displayOnToggleState(dbg, "renderMode", false);

            Group ig = graphicEntityModule.createGroup(sp, dbg)
                .setX(cellX(item.x))
                .setY(cellY(item.y))
                .setZIndex(5);
            item.group = ig;
            updateItemTooltip(item);
        }

        // Avatar (Dual-Mode: RPG Sprite + Torchlight Halo vs Geometric Circle)
        torchHalo = graphicEntityModule.createCircle()
            .setRadius(140)
            .setFillColor(0xf6e05e)
            .setFillAlpha(0.14)
            .setLineWidth(2)
            .setLineColor(0xf6e05e)
            .setLineAlpha(0.35);

        Sprite avSprite = graphicEntityModule.createSprite()
            .setImage("hero_fighter.png")
            .setAnchor(0.5)
            .setBaseWidth(58)
            .setBaseHeight(58);

        hpBarFill = graphicEntityModule.createRectangle()
            .setX(-24)
            .setY(-36)
            .setWidth(48)
            .setHeight(7)
            .setFillColor(0x48bb78);

        Group avSpriteView = graphicEntityModule.createGroup(torchHalo, avSprite, hpBarFill);
        toggleModule.displayOnToggleState(avSpriteView, "renderMode", true);

        Circle avGeo = graphicEntityModule.createCircle()
            .setRadius(26)
            .setFillColor(0x3182ce)
            .setLineWidth(4)
            .setLineColor(0xffffff);
        avatarDebugText = graphicEntityModule.createText("AV")
            .setAnchor(0.5)
            .setFontFamily("Lato")
            .setFontSize(36)
            .setFillColor(0xffffff);
        Group avDebugView = graphicEntityModule.createGroup(avGeo, avatarDebugText);
        toggleModule.displayOnToggleState(avDebugView, "renderMode", false);

        avatarGroup = graphicEntityModule.createGroup(avSpriteView, avDebugView)
            .setX(cellX(avatarX))
            .setY(cellY(avatarY))
            .setZIndex(12);

        fxSpell = graphicEntityModule.createSprite()
            .setImage("fx_fireball.png")
            .setAnchor(0.5)
            .setBaseWidth(76)
            .setBaseHeight(76)
            .setVisible(false)
            .setZIndex(18);
    }

    private void updateItemTooltip(DungeonItem item) {
        tooltipModule.setTooltipText(item.group,
            String.format("Entity %d (%c) at (%d, %d)\nValue: %d Gold | HP: %d",
                item.id, item.type, item.x, item.y, item.value, item.hp));
    }

    @Override
    public void gameTurn(int turn) {
        turnText.setText("TURN " + turn + " / " + MAX_TURNS);
        fxSpell.setVisible(false);
        torch = Math.max(0, torch - 1);

        Player player = gameManager.getPlayer();
        sendTurnInput(player);
        player.execute();

        try {
            String line = player.getOutputs().get(0).trim();
            processCommand(line);
        } catch (TimeoutException e) {
            gameManager.putMetadata("Gold", 0);
            gameManager.loseGame("Avatar timed out in the dark!");
            return;
        }

        // Adjacent monsters attack the Avatar
        for (DungeonItem item : items) {
            if (item.active && item.hp > 0 && Math.abs(item.x - avatarX) + Math.abs(item.y - avatarY) <= 1) {
                hp = Math.max(0, hp - 4);
            }
        }

        // Collect items on current tile
        boolean reachedExit = false;
        for (DungeonItem item : items) {
            if (!item.active || item.x != avatarX || item.y != avatarY) continue;
            if (item.type == 'C' || (item.type == 'R' && item.hp == 0)) {
                gold += item.value;
                item.active = false;
                item.group.setVisible(false);
                gameManager.addToGameSummary(GameManager.formatSuccessMessage("Collected " + item.value + " Gold!"));
            } else if (item.type == 'F') {
                hp = Math.min(maxHp, hp + 18);
                torch += 10;
                item.active = false;
                item.group.setVisible(false);
                gameManager.addToGameSummary(GameManager.formatSuccessMessage("Drank from Fountain (+18 HP, +10 Torch)!"));
            } else if (item.type == 'E') {
                gold += item.value + torch;
                reachedExit = true;
            }
        }

        refreshGraphics();

        if (reachedExit) {
            gameManager.putMetadata("Gold", gold);
            gameManager.winGame("Avatar extracted with " + gold + " Gold!");
        } else if (hp <= 0 || torch <= 0) {
            gameManager.putMetadata("Gold", gold / 2);
            gameManager.loseGame(hp <= 0 ? "Avatar fell in combat!" : "Torch went out in the abyss!");
        }
    }

    private void sendTurnInput(Player player) {
        player.sendInputLine(String.format("%d %d %d %d %d %d", avatarX, avatarY, hp, mp, torch, gold));
        for (int y = 0; y < HEIGHT; y++) {
            player.sendInputLine(new String(grid[y]));
        }
        List<DungeonItem> active = new ArrayList<>();
        for (DungeonItem item : items) {
            if (item.active) active.add(item);
        }
        player.sendInputLine(String.valueOf(active.size()));
        for (DungeonItem item : active) {
            player.sendInputLine(String.format("%d %c %d %d %d %d",
                item.id, item.type, item.x, item.y, item.value, item.hp));
        }
    }

    private int[] nextStep(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return new int[]{sx, sy};
        int[][] dist = new int[HEIGHT][WIDTH];
        int[][][] first = new int[HEIGHT][WIDTH][2];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) dist[y][x] = -1;
        }
        Queue<int[]> q = new ArrayDeque<>();
        dist[sy][sx] = 0;
        q.add(new int[]{sx, sy});
        int[] best = {sx, sy};
        int bestM = Math.abs(sx - tx) + Math.abs(sy - ty);

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int cx = cur[0], cy = cur[1];
            if (cx == tx && cy == ty) return first[cy][cx];
            for (int d = 0; d < 4; d++) {
                int nx = cx + DX[d], ny = cy + DY[d];
                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && grid[ny][nx] == '.' && dist[ny][nx] == -1) {
                    dist[ny][nx] = dist[cy][cx] + 1;
                    first[ny][nx][0] = (cx == sx && cy == sy) ? nx : first[cy][cx][0];
                    first[ny][nx][1] = (cx == sx && cy == sy) ? ny : first[cy][cx][1];
                    q.add(new int[]{nx, ny});
                    int m = Math.abs(nx - tx) + Math.abs(ny - ty);
                    if (m < bestM) {
                        bestM = m;
                        best = first[ny][nx];
                    }
                }
            }
        }
        return best;
    }

    private void processCommand(String line) {
        String[] tokens = line.split("\\s+");
        int c = 0;
        String cmd = tokens[c++].toUpperCase();
        if ("MOVE".equals(cmd) && c + 1 < tokens.length) {
            int tx = Integer.parseInt(tokens[c++]);
            int ty = Integer.parseInt(tokens[c++]);
            int[] step = nextStep(avatarX, avatarY, tx, ty);
            avatarX = step[0];
            avatarY = step[1];
            avatarGroup.setX(cellX(avatarX), Curve.EASE_IN_AND_OUT)
                       .setY(cellY(avatarY), Curve.EASE_IN_AND_OUT);
        }

        if (c < tokens.length || !"MOVE".equals(cmd)) {
            String act = "MOVE".equals(cmd) ? tokens[c++].toUpperCase() : cmd;
            if ("STRIKE".equals(act) && c + 1 < tokens.length) {
                int tx = Integer.parseInt(tokens[c++]);
                int ty = Integer.parseInt(tokens[c++]);
                damageMonsterAt(tx, ty, 8, 1);
            } else if ("FLAM_POR".equals(act) && c + 1 < tokens.length && mp >= 5) {
                int tx = Integer.parseInt(tokens[c++]);
                int ty = Integer.parseInt(tokens[c++]);
                mp -= 5;
                damageMonsterAt(tx, ty, 15, 3);
                fxSpell.setImage("fx_fireball.png").setX(cellX(tx)).setY(cellY(ty)).setVisible(true);
            } else if ("MANI".equals(act) && mp >= 4) {
                mp -= 4;
                hp = Math.min(maxHp, hp + 14);
                fxSpell.setImage("fx_heal.png").setX(cellX(avatarX)).setY(cellY(avatarY)).setVisible(true);
            }
        }
    }

    private void damageMonsterAt(int tx, int ty, int dmg, int maxRange) {
        if (Math.abs(avatarX - tx) + Math.abs(avatarY - ty) > maxRange) return;
        for (DungeonItem item : items) {
            if (item.active && item.hp > 0 && item.x == tx && item.y == ty) {
                item.hp = Math.max(0, item.hp - dmg);
                if (item.hp == 0 && item.type == 'M') {
                    gold += item.value;
                    item.active = false;
                    item.group.setVisible(false);
                }
            }
        }
    }

    private void refreshGraphics() {
        hudStatsText.setText(String.format("HP: %d | MP: %d | TORCH: %d | GOLD: %d", hp, mp, torch, gold));
        hpBarFill.setWidth(Math.max(4, (int) Math.round(48.0 * hp / maxHp)));
        torchHalo.setRadius(Math.max(45, Math.min(160, torch * 2 + 20)));
        tooltipModule.setTooltipText(avatarGroup,
            String.format("Avatar (%d, %d)\nHP: %d/%d | MP: %d\nTorch: %d | Gold: %d",
                avatarX, avatarY, hp, maxHp, mp, torch, gold));
    }

    @Override
    public void onEnd() {
        gameManager.putMetadata("Gold", gold);
    }
}
