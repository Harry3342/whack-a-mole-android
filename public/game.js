// Whack A Mole - Web Arcade Engine

(function () {
  'use strict';

  // --- SVG Mole Graphic Template ---
  function getMoleSvgHtml(isBonked) {
    return `
      <svg class="mole-svg" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
        <!-- Body -->
        <ellipse cx="50" cy="65" rx="34" ry="40" fill="url(#moleBodyGrad)" />
        <ellipse cx="50" cy="72" rx="22" ry="24" fill="#bcaaa4" opacity="0.3" />

        <!-- Yellow Safety Hard Hat -->
        <path d="M 26 32 A 24 20 0 0 1 74 32 Z" fill="url(#hatGrad)" />
        <rect x="22" y="30" width="56" height="6" rx="3" fill="#f57f17" />

        <!-- Eyes -->
        ${
          isBonked
            ? `
          <!-- Dizzy 'X X' Eyes -->
          <line x1="34" y1="40" x2="44" y2="48" stroke="#e53935" stroke-width="3.5" stroke-linecap="round" />
          <line x1="44" y1="40" x2="34" y2="48" stroke="#e53935" stroke-width="3.5" stroke-linecap="round" />
          <line x1="56" y1="40" x2="66" y2="48" stroke="#e53935" stroke-width="3.5" stroke-linecap="round" />
          <line x1="66" y1="40" x2="56" y2="48" stroke="#e53935" stroke-width="3.5" stroke-linecap="round" />
        `
            : `
          <!-- Big Cheerful Eyes -->
          <ellipse cx="39" cy="44" rx="6.5" ry="7.5" fill="#ffffff" />
          <circle cx="40" cy="44" r="4.5" fill="#1e1e1e" />
          <circle cx="41.5" cy="42.5" r="1.6" fill="#ffffff" />

          <ellipse cx="61" cy="44" rx="6.5" ry="7.5" fill="#ffffff" />
          <circle cx="60" cy="44" r="4.5" fill="#1e1e1e" />
          <circle cx="61.5" cy="42.5" r="1.6" fill="#ffffff" />
        `
        }

        <!-- Rosy Cheeks -->
        <circle cx="28" cy="52" r="6.5" fill="#ff8a80" opacity="0.55" />
        <circle cx="72" cy="52" r="6.5" fill="#ff8a80" opacity="0.55" />

        <!-- Snout & Nose -->
        <ellipse cx="50" cy="53" rx="13" ry="8.5" fill="#ffab91" />
        <ellipse cx="50" cy="51" rx="6.5" ry="4" fill="#3e2723" />
        <path d="M 45 56 Q 50 59 55 56" stroke="#3e2723" stroke-width="2.5" fill="none" stroke-linecap="round" />

        <!-- Paws -->
        <ellipse cx="22" cy="68" rx="9" ry="6" fill="#ffab91" />
        <ellipse cx="78" cy="68" rx="9" ry="6" fill="#ffab91" />

        <defs>
          <linearGradient id="moleBodyGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#8d5b4c" />
            <stop offset="100%" stop-color="#5d382b" />
          </linearGradient>
          <linearGradient id="hatGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#ffee58" />
            <stop offset="60%" stop-color="#fbc02d" />
            <stop offset="100%" stop-color="#f57f17" />
          </linearGradient>
        </defs>
      </svg>
    `;
  }

  // --- Web Audio Synthesizer (Zero External Dependencies) ---
  class SoundManager {
    constructor() {
      this.ctx = null;
      this.enabled = localStorage.getItem('whack_mole_sound') !== 'false';
    }

    init() {
      if (!this.ctx) {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (AudioCtx) this.ctx = new AudioCtx();
      }
      if (this.ctx && this.ctx.state === 'suspended') {
        this.ctx.resume();
      }
    }

    toggle() {
      this.enabled = !this.enabled;
      localStorage.setItem('whack_mole_sound', this.enabled);
      return this.enabled;
    }

    playBonk() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(380, now);
        osc.frequency.exponentialRampToValueAtTime(80, now + 0.14);

        gain.gain.setValueAtTime(0.7, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.14);

        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.15);
      } catch (_) {}
    }

    playMiss() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = 'sine';
        osc.frequency.setValueAtTime(220, now);
        osc.frequency.exponentialRampToValueAtTime(110, now + 0.1);

        gain.gain.setValueAtTime(0.3, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);

        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.11);
      } catch (_) {}
    }

    playTick() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      try {
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = 'sine';
        osc.frequency.setValueAtTime(600, now);

        gain.gain.setValueAtTime(0.3, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.05);

        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now);
        osc.stop(now + 0.06);
      } catch (_) {}
    }

    playFanfare() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      try {
        const notes = [261.63, 329.63, 392.0, 523.25];
        notes.forEach((freq, index) => {
          const now = this.ctx.currentTime + index * 0.12;
          const osc = this.ctx.createOscillator();
          const gain = this.ctx.createGain();
          osc.type = 'sine';
          osc.frequency.setValueAtTime(freq, now);
          gain.gain.setValueAtTime(0.4, now);
          gain.gain.exponentialRampToValueAtTime(0.01, now + 0.25);
          osc.connect(gain);
          gain.connect(this.ctx.destination);
          osc.start(now);
          osc.stop(now + 0.26);
        });
      } catch (_) {}
    }
  }

  // --- High Score Manager (Persistent via localStorage) ---
  class ScoreManager {
    static getScores() {
      try {
        const data = localStorage.getItem('whack_mole_highscores');
        return data ? JSON.parse(data) : [];
      } catch (_) {
        return [];
      }
    }

    static getBestScore() {
      const list = this.getScores();
      return list.length > 0 ? list[0].score : 0;
    }

    static saveScore(entry) {
      try {
        const list = this.getScores();
        list.push(entry);
        list.sort((a, b) => b.score - a.score);
        const top10 = list.slice(0, 10);
        localStorage.setItem('whack_mole_highscores', JSON.stringify(top10));
        return top10;
      } catch (_) {
        return [];
      }
    }

    static clearScores() {
      try {
        localStorage.removeItem('whack_mole_highscores');
      } catch (_) {}
    }
  }

  // --- Game Engine State ---
  const Difficulties = {
    CASUAL: { name: 'Casual', interval: 700 },
    CLASSIC: { name: 'Classic', interval: 500 },
    BLITZ: { name: 'Blitz', interval: 360 }
  };

  const state = {
    gameState: 'IDLE', // IDLE, COUNTDOWN, PLAYING, PAUSED, GAME_OVER
    difficulty: 'CLASSIC',
    score: 0,
    timeLeft: 60,
    activeHoleIndex: null,
    whackedHoleIndex: null,
    currentCombo: 0,
    maxCombo: 0,
    totalHits: 0,
    totalTaps: 0,
    moleTimer: null,
    gameTimer: null,
    countdownTimer: null
  };

  const sound = new SoundManager();

  // --- DOM Elements ---
  const dom = {
    grid: document.getElementById('mole-grid'),
    hud: document.getElementById('game-hud'),
    hudScore: document.getElementById('hud-score'),
    hudBestScore: document.getElementById('hud-best-score'),
    hudTimer: document.getElementById('hud-timer'),
    hudComboBadge: document.getElementById('hud-combo-badge'),
    timerCapsule: document.getElementById('timer-capsule'),
    timerProgressBar: document.getElementById('timer-progress-bar'),
    gameplayTip: document.getElementById('gameplay-tip'),
    btnSound: document.getElementById('btn-sound'),
    btnPause: document.getElementById('btn-pause'),
    btnStart: document.getElementById('btn-start'),
    btnResume: document.getElementById('btn-resume'),
    btnRestart: document.getElementById('btn-restart'),
    btnQuit: document.getElementById('btn-quit'),
    btnPlayAgain: document.getElementById('btn-play-again'),
    btnGameOverMenu: document.getElementById('btn-gameover-menu'),
    btnGameOverScores: document.getElementById('btn-gameover-scores'),
    btnShowScores: document.getElementById('btn-show-scores'),
    btnCloseLeaderboard: document.getElementById('btn-close-leaderboard'),
    btnClearScores: document.getElementById('btn-clear-scores'),
    btnLeaderboardTop: document.getElementById('btn-leaderboard-top'),
    startOverlay: document.getElementById('start-overlay'),
    countdownOverlay: document.getElementById('countdown-overlay'),
    countdownNumber: document.getElementById('countdown-number'),
    pauseOverlay: document.getElementById('pause-overlay'),
    gameoverOverlay: document.getElementById('gameover-overlay'),
    leaderboardOverlay: document.getElementById('leaderboard-overlay'),
    startBestScore: document.getElementById('start-best-score'),
    finalScore: document.getElementById('final-score'),
    recordBanner: document.getElementById('record-banner'),
    statAccuracy: document.getElementById('stat-accuracy'),
    statHits: document.getElementById('stat-hits'),
    statCombo: document.getElementById('stat-combo'),
    statPph: document.getElementById('stat-pph'),
    leaderboardList: document.getElementById('leaderboard-list'),
    diffButtons: document.querySelectorAll('.diff-btn')
  };

  const holeElements = [];

  // --- Initialize 3x3 Grid DOM ---
  function createGrid() {
    dom.grid.innerHTML = '';
    holeElements.length = 0;

    for (let i = 0; i < 9; i++) {
      const hole = document.createElement('div');
      hole.className = 'hole-cell';
      hole.dataset.index = i;

      hole.innerHTML = `
        <div class="cavern-mask">
          <div class="mole-creature">
            ${getMoleSvgHtml(false)}
          </div>
        </div>
        <div class="hole-front-lip"></div>
        <div class="pow-badge">POW!</div>
        <div class="dizzy-stars">⭐</div>
        <div class="mallet-strike">🔨</div>
      `;

      hole.addEventListener('pointerdown', (e) => {
        e.preventDefault();
        onHoleClick(i);
      });

      dom.grid.appendChild(hole);
      holeElements.push(hole);
    }
  }

  // --- Sound toggle UI sync ---
  function updateSoundButton() {
    dom.btnSound.textContent = sound.enabled ? '🔊' : '🔇';
  }

  dom.btnSound.addEventListener('click', () => {
    sound.toggle();
    updateSoundButton();
  });
  updateSoundButton();

  // --- Difficulty Selection ---
  dom.diffButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      dom.diffButtons.forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      state.difficulty = btn.dataset.diff;
    });
  });

  // --- Vibrate Helper ---
  function vibrate(pattern) {
    if (navigator.vibrate) {
      try {
        navigator.vibrate(pattern);
      } catch (_) {}
    }
  }

  // --- Game Flow: Start -> Countdown -> Play -> Game Over ---
  function prepareAndStartGame() {
    sound.init();
    hideAllOverlays();
    dom.countdownOverlay.classList.remove('hidden');
    dom.countdownOverlay.classList.add('active');

    let count = 3;
    dom.countdownNumber.textContent = count;
    sound.playTick();
    vibrate(30);

    state.countdownTimer = setInterval(() => {
      count--;
      if (count > 0) {
        dom.countdownNumber.textContent = count;
        sound.playTick();
        vibrate(30);
      } else {
        clearInterval(state.countdownTimer);
        dom.countdownOverlay.classList.remove('active');
        dom.countdownOverlay.classList.add('hidden');
        startPlaySession();
      }
    }, 800);
  }

  function startPlaySession() {
    state.gameState = 'PLAYING';
    state.score = 0;
    state.timeLeft = 60;
    state.currentCombo = 0;
    state.maxCombo = 0;
    state.totalHits = 0;
    state.totalTaps = 0;
    state.activeHoleIndex = null;
    state.whackedHoleIndex = null;

    updateHud();
    dom.hud.classList.remove('hidden');
    dom.gameplayTip.classList.remove('hidden');

    startMoleSpawner();
    startGameTimer();
  }

  function startMoleSpawner() {
    clearInterval(state.moleTimer);
    const interval = Difficulties[state.difficulty].interval;

    state.moleTimer = setInterval(() => {
      if (state.gameState !== 'PLAYING') return;

      // Deselect previous
      if (state.activeHoleIndex !== null) {
        setHoleState(state.activeHoleIndex, false, false);
      }

      // Pick new random hole (0..8)
      let nextIndex = Math.floor(Math.random() * 9);
      if (nextIndex === state.activeHoleIndex) {
        nextIndex = (nextIndex + 1 + Math.floor(Math.random() * 8)) % 9;
      }

      state.activeHoleIndex = nextIndex;
      setHoleState(nextIndex, true, false);
    }, interval);
  }

  function startGameTimer() {
    clearInterval(state.gameTimer);
    state.gameTimer = setInterval(() => {
      if (state.gameState !== 'PLAYING') return;

      state.timeLeft--;
      updateHud();

      if (state.timeLeft <= 5 && state.timeLeft > 0) {
        sound.playTick();
        vibrate(25);
      }

      if (state.timeLeft <= 0) {
        triggerGameOver();
      }
    }, 1000);
  }

  function setHoleState(index, isActive, isBonked) {
    const hole = holeElements[index];
    if (!hole) return;

    hole.classList.remove('active', 'bonked');
    const moleCreature = hole.querySelector('.mole-creature');

    if (isActive) {
      hole.classList.add('active');
      moleCreature.innerHTML = getMoleSvgHtml(false);
    } else if (isBonked) {
      hole.classList.add('bonked');
      moleCreature.innerHTML = getMoleSvgHtml(true);
    }
  }

  // --- Tap / Whack Interaction ---
  function onHoleClick(index) {
    if (state.gameState !== 'PLAYING') return;

    state.totalTaps++;
    const hole = holeElements[index];
    const isHit = state.activeHoleIndex === index;

    // Trigger visual mallet strike on tap
    showMalletFeedback(hole, isHit);

    if (isHit) {
      // SUCCESSFUL HIT
      state.totalHits++;
      state.currentCombo++;
      state.maxCombo = Math.max(state.maxCombo, state.currentCombo);

      const multiplier = state.currentCombo >= 7 ? 3 : state.currentCombo >= 4 ? 2 : 1;
      const points = 10 * multiplier;
      state.score += points;

      sound.playBonk();
      vibrate(45);

      // Show bonked visual
      state.activeHoleIndex = null;
      setHoleState(index, false, true);

      // Floating score popup
      const floatText = multiplier > 1 ? `+${points} (x${multiplier}!)` : `+${points}`;
      showFloatingScore(hole, floatText, multiplier > 1);

      // Hide mole after 300ms
      setTimeout(() => {
        setHoleState(index, false, false);
      }, 300);
    } else {
      // MISS
      state.currentCombo = 0;
      sound.playMiss();
      vibrate(20);
    }

    updateHud();
  }

  function showMalletFeedback(hole, isHit) {
    const mallet = hole.querySelector('.mallet-strike');
    mallet.textContent = isHit ? '🔨' : '💨';
    hole.classList.add('strike');
    setTimeout(() => {
      hole.classList.remove('strike');
    }, 180);
  }

  function showFloatingScore(hole, text, isBonus) {
    const popup = document.createElement('div');
    popup.className = `floating-score ${isBonus ? 'bonus' : ''}`;
    popup.textContent = text;
    hole.appendChild(popup);
    setTimeout(() => {
      popup.remove();
    }, 500);
  }

  // --- Update HUD Display ---
  function updateHud() {
    dom.hudScore.textContent = state.score;
    dom.hudTimer.textContent = `${state.timeLeft}s`;

    const best = Math.max(ScoreManager.getBestScore(), state.score);
    dom.hudBestScore.textContent = best;
    dom.startBestScore.textContent = `${best} pts`;

    // Combo badge
    if (state.currentCombo >= 3) {
      const mult = state.currentCombo >= 7 ? 'x3' : 'x2';
      dom.hudComboBadge.textContent = `🔥 ${mult} (${state.currentCombo} STREAK)`;
      dom.hudComboBadge.classList.remove('hidden');
    } else {
      dom.hudComboBadge.classList.add('hidden');
    }

    // Timer urgency styling
    dom.timerCapsule.classList.remove('warning', 'danger');
    if (state.timeLeft <= 10) {
      dom.timerCapsule.classList.add('danger');
    } else if (state.timeLeft <= 20) {
      dom.timerCapsule.classList.add('warning');
    }

    // Progress bar
    const pct = Math.max(0, Math.min(100, (state.timeLeft / 60) * 100));
    dom.timerProgressBar.style.width = `${pct}%`;
    dom.timerProgressBar.style.background =
      state.timeLeft <= 10 ? 'var(--bonk-red)' : state.timeLeft <= 20 ? 'var(--amber)' : 'var(--primary)';
  }

  // --- Game Over ---
  function triggerGameOver() {
    clearInterval(state.gameTimer);
    clearInterval(state.moleTimer);
    state.gameState = 'GAME_OVER';

    // Clear any active mole
    if (state.activeHoleIndex !== null) {
      setHoleState(state.activeHoleIndex, false, false);
      state.activeHoleIndex = null;
    }

    vibrate([60, 60, 120]);
    sound.playFanfare();

    const bestBefore = ScoreManager.getBestScore();
    const isNewRecord = state.score > 0 && state.score > bestBefore;

    const accuracy = state.totalTaps > 0 ? Math.round((state.totalHits / state.totalTaps) * 100) : 100;
    const pph = state.totalHits > 0 ? (state.score / state.totalHits).toFixed(1) : 0;

    // Save to localStorage Hall of Fame
    ScoreManager.saveScore({
      score: state.score,
      accuracy: accuracy,
      hits: state.totalHits,
      maxCombo: state.maxCombo,
      difficulty: Difficulties[state.difficulty].name,
      date: new Date().toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
    });

    // Populate scorecard
    dom.finalScore.textContent = state.score;
    dom.statAccuracy.textContent = `${accuracy}%`;
    dom.statHits.textContent = state.totalHits;
    dom.statCombo.textContent = `${state.maxCombo}x`;
    dom.statPph.textContent = pph;

    if (isNewRecord) {
      dom.recordBanner.classList.remove('hidden');
    } else {
      dom.recordBanner.classList.add('hidden');
    }

    hideAllOverlays();
    dom.gameoverOverlay.classList.remove('hidden');
    dom.gameoverOverlay.classList.add('active');
  }

  // --- Pause & Resume ---
  function pauseGame() {
    if (state.gameState !== 'PLAYING') return;
    state.gameState = 'PAUSED';
    clearInterval(state.gameTimer);
    clearInterval(state.moleTimer);
    dom.pauseOverlay.classList.remove('hidden');
    dom.pauseOverlay.classList.add('active');
  }

  function resumeGame() {
    if (state.gameState !== 'PAUSED') return;
    state.gameState = 'PLAYING';
    dom.pauseOverlay.classList.remove('active');
    dom.pauseOverlay.classList.add('hidden');
    startMoleSpawner();
    startGameTimer();
  }

  function quitToMenu() {
    clearInterval(state.gameTimer);
    clearInterval(state.moleTimer);
    clearInterval(state.countdownTimer);
    state.gameState = 'IDLE';

    if (state.activeHoleIndex !== null) {
      setHoleState(state.activeHoleIndex, false, false);
      state.activeHoleIndex = null;
    }

    hideAllOverlays();
    dom.hud.classList.add('hidden');
    dom.gameplayTip.classList.add('hidden');
    dom.startOverlay.classList.remove('hidden');
    dom.startOverlay.classList.add('active');

    const best = ScoreManager.getBestScore();
    dom.startBestScore.textContent = `${best} pts`;
  }

  // --- Leaderboard Overlay ---
  function renderLeaderboard() {
    const list = ScoreManager.getScores();
    dom.leaderboardList.innerHTML = '';

    if (list.length === 0) {
      dom.leaderboardList.innerHTML = `
        <div style="padding: 24px 0; color: #78909c;">
          <p style="font-size: 1.1rem; font-weight: 700;">No High Scores Yet!</p>
          <p style="font-size: 0.85rem;">Play a game to claim your spot in the Hall of Fame.</p>
        </div>
      `;
      return;
    }

    list.forEach((item, index) => {
      const rank = index + 1;
      const rankClass = rank === 1 ? 'gold' : rank === 2 ? 'silver' : rank === 3 ? 'bronze' : '';

      const row = document.createElement('div');
      row.className = 'score-row';
      row.innerHTML = `
        <div class="score-row-left">
          <div class="rank-badge ${rankClass}">${rank}</div>
          <div class="score-info">
            <div class="score-main">
              <span class="score-points">${item.score} pts</span>
              <span class="diff-tag">${item.difficulty || 'Classic'}</span>
            </div>
            <div class="score-sub">${item.date || 'Recent'} • ${item.accuracy || 100}% Acc • ${item.maxCombo || 0}x Streak</div>
          </div>
        </div>
        <div style="font-weight: 700; color: #78909c;">${item.hits || 0} 🔨</div>
      `;
      dom.leaderboardList.appendChild(row);
    });
  }

  function showLeaderboard() {
    renderLeaderboard();
    dom.leaderboardOverlay.classList.remove('hidden');
    dom.leaderboardOverlay.classList.add('active');
  }

  function hideLeaderboard() {
    dom.leaderboardOverlay.classList.remove('active');
    dom.leaderboardOverlay.classList.add('hidden');
  }

  function hideAllOverlays() {
    [dom.startOverlay, dom.countdownOverlay, dom.pauseOverlay, dom.gameoverOverlay, dom.leaderboardOverlay].forEach(
      (el) => {
        el.classList.remove('active');
        el.classList.add('hidden');
      }
    );
  }

  // --- Attach Event Listeners ---
  dom.btnStart.addEventListener('click', prepareAndStartGame);
  dom.btnPause.addEventListener('click', pauseGame);
  dom.btnResume.addEventListener('click', resumeGame);
  dom.btnRestart.addEventListener('click', prepareAndStartGame);
  dom.btnQuit.addEventListener('click', quitToMenu);
  dom.btnPlayAgain.addEventListener('click', prepareAndStartGame);
  dom.btnGameOverMenu.addEventListener('click', quitToMenu);
  dom.btnGameOverScores.addEventListener('click', showLeaderboard);
  dom.btnShowScores.addEventListener('click', showLeaderboard);
  dom.btnLeaderboardTop.addEventListener('click', showLeaderboard);
  dom.btnCloseLeaderboard.addEventListener('click', hideLeaderboard);

  dom.btnClearScores.addEventListener('click', () => {
    if (confirm('Are you sure you want to clear all high scores?')) {
      ScoreManager.clearScores();
      renderLeaderboard();
      updateHud();
    }
  });

  // Close overlays on outside click
  dom.leaderboardOverlay.addEventListener('click', (e) => {
    if (e.target === dom.leaderboardOverlay) hideLeaderboard();
  });

  // --- Initial Setup ---
  createGrid();
  quitToMenu();
})();
