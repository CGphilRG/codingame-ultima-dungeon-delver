# Ultima Dungeon Delver — CodinGame Optimization RPG SDK Project

Un puzzle d'optimisation solo inspiré d'**Ultima IV Maker** et **Dungeon Crawler**, avec bascule dynamique PixiJS entre **Mode Sprites RPG 2D (avec halo de torche dynamique)** et **Mode Formes Géométriques Debug** (`ToggleModule`).

## Commandes Rapides
* **Viewer Web Local (`http://localhost:8888/test.html`)** :
  ```bash
  cd ultima-dungeon-delver-sdk
  mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test"
  ```
* **Simulation Batch (3 donjons validateurs)** :
  ```bash
  cd ultima-dungeon-delver-sdk
  mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test" -Dexec.args="--simulate"
  ```

## CodinGame & GitHub Links

- **CodinGame Contribution**: https://www.codingame.com/contribute/view/158680d08ca9d3d2323c3495364e30863bbe90
- **Play in CodinGame IDE**: https://www.codingame.com/ide/demo/1605775f633efb9245a42848603a5d4018881dc
- **GitHub Repository**: https://github.com/CGphilRG/codingame-ultima-dungeon-delver
- **6-Puzzle Showcase Hub (GitHub Pages)**: https://cgphilrg.github.io/codingame-sdk-puzzles/
