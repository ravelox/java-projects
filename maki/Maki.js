class Maki {
    constructor(canvas) {
        this.MAX_X = 14;
        this.MAX_Y = 14;
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.board = Array.from({ length: this.MAX_X }, () => Array(this.MAX_Y).fill(0));
        this.marker = Array.from({ length: this.MAX_X }, () => Array(this.MAX_Y).fill(0));
        this.undoBoard = Array.from({ length: this.MAX_X }, () => Array(this.MAX_Y).fill(0));
        this.gameOver = false;
        this.score = 0;
        this.undoScore = 0;
        this.initialiseBoard();
        canvas.addEventListener('click', this.mouseClicked.bind(this));
        this.paint();
        this.drawScore();
    }

    initialiseBoard() {
        for (let x = 0; x < this.MAX_X; x++) {
            for (let y = 0; y < this.MAX_Y; y++) {
                this.marker[x][y] = 0;
                this.board[x][y] = Math.floor(Math.random() * 5) + 1;
            }
        }
        this.gameOver = false;
        this.score = 0;
        this.undoScore = 0;
    }

    paint() {
        for (let x = 0; x < this.MAX_X; x++) {
            for (let y = 0; y < this.MAX_Y; y++) {
                if (this.marker[x][y] > 0) {
                    this.ctx.fillStyle = 'black';
                } else {
                    switch (this.board[x][y]) {
                        case 1: this.ctx.fillStyle = 'red'; break;
                        case 2: this.ctx.fillStyle = 'yellow'; break;
                        case 3: this.ctx.fillStyle = 'blue'; break;
                        case 4: this.ctx.fillStyle = 'white'; break;
                        case 5: this.ctx.fillStyle = 'green'; break;
                        default: this.ctx.fillStyle = 'white'; break;
                    }
                }
                if (this.board[x][y] > 0) {
                    this.ctx.fillRect((x * 25) + 1, (y * 25) + 1, 23, 23);
                    this.ctx.strokeRect((x * 25) + 1, (y * 25) + 1, 24, 24);
                } else {
                    this.ctx.clearRect((x * 25) + 1, (y * 25) + 1, 25, 25);
                }
            }
        }
        if (this.gameOver) {
            this.ctx.fillStyle = 'black';
            this.ctx.fillText('GAME OVER', 50, 50);
        }
    }

    drawScore() {
        this.ctx.clearRect((this.MAX_X + 1) * 25, 0, 100, 100);
        this.ctx.fillStyle = 'black';
        this.ctx.fillText(String(this.score), (this.MAX_X + 1) * 25, 20);
    }

    mouseClicked(e) {
        const boxX = Math.floor(e.offsetX / 25);
        const boxY = Math.floor(e.offsetY / 25);
        let boxes_removed;
        if (this.gameOver) return;
        if (boxX > this.MAX_X || boxY > this.MAX_Y) return;
        if (this.board[boxX][boxY] === 0) {
            this.clear_boxes(true);
            return;
        }
        if (this.marker[boxX][boxY] > 0) {
            boxes_removed = this.count_marked();
            this.score = this.score + Math.pow((boxes_removed - 2), 2);
            this.drawScore();
            this.clear_boxes(false);
            this.pack_columns();
            this.shift_columns();
            this.gameOver = this.check_win();
        } else {
            this.clear_boxes(true);
            this.saveUndo();
            this.mark_boxes(boxX, boxY, this.board[boxX][boxY]);
            if (this.count_marked() < 2) this.clear_boxes(true);
        }
        this.paint();
    }

    mark_boxes(x, y, color) {
        if (x < 0 || y < 0 || x >= this.MAX_X || y >= this.MAX_Y) return;
        if (this.marker[x][y] > 0 || this.board[x][y] !== color) return;
        this.marker[x][y] = 1;
        this.mark_boxes(x - 1, y, color);
        this.mark_boxes(x + 1, y, color);
        this.mark_boxes(x, y - 1, color);
        this.mark_boxes(x, y + 1, color);
    }

    clear_boxes(reset) {
        for (let x = 0; x < this.MAX_X; x++) {
            for (let y = 0; y < this.MAX_Y; y++) {
                if (this.marker[x][y] > 0) {
                    if (!reset) this.board[x][y] = 0;
                    this.marker[x][y] = 0;
                }
            }
        }
    }

    count_marked() {
        let marked = 0;
        for (let x = 0; x < this.MAX_X; x++) {
            for (let y = 0; y < this.MAX_Y; y++) {
                if (this.marker[x][y] > 0) marked++;
            }
        }
        return marked;
    }

    pack_columns() {
        for (let y = this.MAX_Y - 1; y >= 0; y--) {
            for (let x = 0; x < this.MAX_X; x++) {
                let j = y;
                while (j < this.MAX_Y - 1) {
                    if (this.board[x][j + 1] === 0) {
                        this.board[x][j + 1] = this.board[x][j];
                        this.board[x][j] = 0;
                    }
                    j++;
                }
            }
        }
    }

    shift_columns() {
        for (let x = 1; x < this.MAX_X; x++) {
            let j = x;
            while (j > 0 && this.board[j - 1][this.MAX_Y - 1] === 0) {
                for (let y = 0; y < this.MAX_Y; y++) {
                    this.board[j - 1][y] = this.board[j][y];
                    this.board[j][y] = 0;
                }
                j--;
            }
        }
    }

    check_win() {
        let x = 0;
        let y = this.MAX_Y - 1;
        while (y >= 0) {
            if (this.board[x][y] > 0) {
                this.mark_boxes(x, y, this.board[x][y]);
                const marked = this.count_marked();
                this.clear_boxes(true);
                if (marked > 1) return false;
            }
            x++;
            if (x > this.MAX_X - 1) {
                x = 0;
                y--;
            }
        }
        return true;
    }

    reset() {
        this.initialiseBoard();
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.paint();
        this.drawScore();
    }

    saveUndo() {
        for (let x = 0; x < this.MAX_X; x++) {
            for (let y = 0; y < this.MAX_X; y++) {
                this.undoBoard[x][y] = this.board[x][y];
            }
        }
        this.undoScore = this.score;
    }

    undo() {
        for (let x = 0; x < this.MAX_X; x++) {
            for (let y = 0; y < this.MAX_X; y++) {
                this.board[x][y] = this.undoBoard[x][y];
            }
        }
        this.score = this.undoScore;
        this.paint();
        this.drawScore();
    }
}

// Expose the class when running in a browser without modules
if (typeof window !== 'undefined') {
    window.Maki = Maki;
}
