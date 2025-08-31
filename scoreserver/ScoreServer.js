class ScoreServer {
    constructor() {
        this.games = [];
        window.scoreServer = this;
    }

    findGame(name) {
        return this.games.find(g => g.name.toLowerCase() === name.toLowerCase());
    }

    isHi(game, score) {
        const g = this.findGame(game);
        if (!g) return 0;
        return g.isHiScore(score) ? 1 : 0;
    }

    getTable(game) {
        let g = this.findGame(game);
        if (!g) {
            g = new GameTable(game);
            this.games.push(g);
        }
        return g.getScores();
    }

    addScore(game, name, score) {
        let g = this.findGame(game);
        if (!g) {
            g = new GameTable(game);
            this.games.push(g);
        }
        g.addScore(name, score);
    }

    handle(commandString) {
        const parts = commandString.split('#');
        const command = parts[0].toLowerCase();
        const game = parts[1];
        switch (command) {
            case 'get':
                return this.getTable(game);
            case 'clr':
                const g = this.findGame(game);
                if (g) {
                    this.games = this.games.filter(x => x !== g);
                    this.games.push(new GameTable(game));
                }
                return '';
            case 'chk':
                if (parts.length >= 3) {
                    const score = parseInt(parts[2], 10);
                    return String(this.isHi(game, score));
                }
                return '0';
            case 'add':
                if (parts.length >= 4) {
                    const score = parseInt(parts[2], 10);
                    const name = parts[3];
                    this.addScore(game, name, score);
                }
                return '';
            default:
                return '';
        }
    }
}

class Player {
    constructor(name, score) {
        this.playerName = name;
        this.playerScore = score;
    }
}

class GameTable {
    constructor(name) {
        this.name = name;
        this.playerScores = [];
        for (let i = 0; i < 10; i++) {
            this.playerScores.push(new Player(`${name} ${i + 1}`, 0));
        }
    }

    isHiScore(score) {
        const p = this.playerScores[9];
        return score > p.playerScore;
    }

    addScore(name, score) {
        const p = new Player(name, score);
        let i = 0;
        while (i < this.playerScores.length && p.playerScore <= this.playerScores[i].playerScore) {
            i++;
        }
        this.playerScores.splice(i, 0, p);
        if (this.playerScores.length > 10) {
            this.playerScores.pop();
        }
    }

    getScores() {
        return this.playerScores
            .slice(0, 10)
            .map(p => `${p.playerName}#${p.playerScore}`)
            .join(';');
    }
}

export { ScoreServer };
