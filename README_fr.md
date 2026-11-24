> 🇬🇧 [English version](README.md)

# Ultima Dungeon Delver — Projet CodinGame Optimization RPG SDK

Un puzzle d'optimisation solo inspiré d'**Ultima IV Maker** et des **Dungeon Crawlers** classiques, avec bascule dynamique PixiJS entre **Mode Sprites RPG 2D (avec halo de torche dynamique)** et **Mode Formes Géométriques Debug** (`ToggleModule`).

---

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
* **Changer de banque de sprites CC0** :
  ```bash
  python3 scripts/apply_sprite_pack.py kenney-tiny-dungeon   # ou kenney-roguelike-field / kenney-1bit-retro / legacy-dark-stone
  ```

---

## Liens CodinGame & GitHub

* **Contribution CodinGame** : https://www.codingame.com/contribute/view/158680d08ca9d3d2323c3495364e30863bbe90
* **Jouer dans l'IDE CodinGame** : https://www.codingame.com/ide/demo/1605775f633efb9245a42848603a5d4018881dc
* **Dépôt GitHub** : https://github.com/CGphilRG/codingame-ultima-dungeon-delver
* **Portail GitHub Pages (6 Puzzles)** : https://cgphilrg.github.io/codingame-sdk-puzzles/
