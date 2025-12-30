# 🟡 Pac-Man Java Swing Game

A fully functional Pac-Man clone built with Java Swing, featuring classic gameplay mechanics.[file:1][file:2]

<div align="center">
  <img src="screenshots/gameplay.gif" alt="Pac-Man Gameplay" width="600"/>
  <br><em>Classic Pac-Man gameplay with 4 ghosts, cherries, scoring & lives</em>
</div>

## ✨ Features

- **Arrow Key Controls** - Smooth Pac-Man movement (↑↓←→)
- **4 Color Ghosts** - Blue, Orange, Pink, Red with scared mode
- **Cherry Power-ups** - Spawn after dots eaten, make ghosts edible (200pts each)
- **Scoring** - 10pts per dot, 200pts per ghost
- **3 Lives System** - Respawn on death, Game Over at 0 lives
- **Game States** - Menu, Playing, Paused, Game Over
- **Level Complete** - Auto-restart when all dots eaten

## 🎮 Controls

| Key     | Action              |
|---------|---------------------|
| **↑↓←→** | Move Pac-Man        |
| **ESC**  | Pause/Resume        |
| **ENTER**| Restart (Game Over) |

## 📁 Project Structure

PacManGame/
├── App.java # Main launcher (JFrame)
├── PacMan.java # Core game (JPanel + logic)
└── Resource/ # Game assets (11 PNGs)
├── pacmanUp.png
├── pacmanDown.png
├── pacmanLeft.png
├── pacmanRight.png
├── blueGhost.png
├── orangeGhost.png
├── pinkGhost.png
├── redGhost.png
├── scaredGhost.png
├── cherry.png
├── wall.png

## 👥 Team

| Member | Responsibilities |
|--------|------------------|
| **Ayoub** | Repo setup, core features, game states |
| **Ahmed** | Pac-Man movement & collision |
| **Aymen** | Ghost AI, map parsing |
| **Amal**  | UI rendering, game loop |

## 🚀 Quick Start

### Prerequisites
- Java 8+ (Tested: OpenJDK 17)

### Run

#### Clone & navigate
git clone https://github.com/AyoubCharbaji/PacManGame.git

cd PacManGame

#### Compile & run
javac *.java
java App


**Or** Open in IntelliJ/VSCode → Run `App.main()`

## 📊 Game Rules

1. **Eat all white dots** → Level complete
2. **Avoid ghosts** → Lose 1 life (unless scared)
3. **Eat cherry** → Ghosts turn blue (15s), eat for 200pts
4. **0 lives** → GAME OVER

## 📈 Development Timeline

- **Week 1**: Repo setup + Pac-Man movement
- **Week 2**: Map parsing + 4 ghosts AI
- **Week 3**: UI rendering + game loop
- **Week 4**: Core features (score/lives/cherries)

**15+ commits** across feature branches with PR reviews.

## 🔮 Future Plans

- 🎵 Sound effects
- 🏆 High score save
- 🌍 Multiple levels
- 🧠 Advanced ghost AI

## 📄 License
MIT License - Free to use & modify [file:1][file:2]
