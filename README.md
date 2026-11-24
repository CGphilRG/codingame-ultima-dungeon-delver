> 🇫🇷 [Version française](README_fr.md)

# Ultima Dungeon Delver — CodinGame Optimization RPG SDK Project

A solo optimization RPG puzzle inspired by **Ultima IV Maker** and classic dungeon crawlers, featuring dynamic PixiJS switching between **2D RPG Sprites Mode (with dynamic torchlight halo)** and **Geometric Debug Mode** (`ToggleModule`).

---

## Quick Commands
* **Local Web Viewer (`http://localhost:8888/test.html`)**:
  ```bash
  cd ultima-dungeon-delver-sdk
  mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test"
  ```
* **Batch Simulation (3 Validator Dungeons)**:
  ```bash
  cd ultima-dungeon-delver-sdk
  mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test" -Dexec.args="--simulate"
  ```
* **Switch CC0 Sprite Pack**:
  ```bash
  python3 scripts/apply_sprite_pack.py kenney-tiny-dungeon   # or kenney-roguelike-field / kenney-1bit-retro / legacy-dark-stone
  ```

---

## CodinGame & GitHub Links

* **CodinGame Contribution**: https://www.codingame.com/contribute/view/158680d08ca9d3d2323c3495364e30863bbe90
* **Play in CodinGame IDE**: https://www.codingame.com/ide/demo/1605775f633efb9245a42848603a5d4018881dc
* **GitHub Repository**: https://github.com/CGphilRG/codingame-ultima-dungeon-delver
* **6-Puzzle Showcase Hub (GitHub Pages)**: https://cgphilrg.github.io/codingame-sdk-puzzles/
