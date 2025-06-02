package com.mycompany.app;

import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgramTest {

    private Game game;
    private Player playerX;
    private Player playerO;
    private char[] board;
    private final ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private TicTacToePanel panel;

    @BeforeEach
    void setup() throws Exception {
        game = new Game();
        playerX = game.player1;
        playerO = game.player2;
        board = new char[9];
        for (int i = 0; i < 9; i++) board[i] = ' ';
        System.setOut(new PrintStream(outputCapture));
        panel = new TicTacToePanel(new GridLayout(3, 3));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void verifyGameStartState() {
        assertNotNull(game);
        assertEquals(State.PLAYING, game.state);
        assertEquals('X', playerX.symbol);
        assertEquals('O', playerO.symbol);
    }

    @Test
    void validateWinningConditionsForX() {
        game.symbol = 'X';
        assertEquals(State.XWIN, game.checkState(new char[]{'X','X','X',' ',' ',' ',' ',' ',' '}));
        assertEquals(State.XWIN, game.checkState(new char[]{'X',' ',' ','X',' ',' ','X',' ',' '}));
        assertEquals(State.XWIN, game.checkState(new char[]{'X',' ',' ',' ','X',' ',' ',' ','X'}));
    }

    @Test
    void validateWinningConditionsForO() {
        game.symbol = 'O';
        assertEquals(State.OWIN, game.checkState(new char[]{'O','O','O',' ',' ',' ',' ',' ',' '}));
        assertEquals(State.OWIN, game.checkState(new char[]{'O',' ',' ','O',' ',' ','O',' ',' '}));
        assertEquals(State.OWIN, game.checkState(new char[]{'O',' ',' ',' ','O',' ',' ',' ','O'}));
    }

    @Test
    void checkDrawCondition() {
        game.symbol = 'X';
        assertEquals(State.DRAW, game.checkState(new char[]{'X','O','X','X','O','O','O','X','X'}));
    }

    @Test
    void checkActivePlayState() {
        game.symbol = 'X';
        assertEquals(State.PLAYING, game.checkState(new char[]{'X','O','X',' ',' ',' ',' ',' ',' '}));
    }

    @Test
    void verifyMoveGeneration() {
        board[0] = 'X';
        board[4] = 'O';
        ArrayList<Integer> availableMoves = new ArrayList<>();
        game.generateMoves(board, availableMoves);
        assertEquals(7, availableMoves.size());
        assertFalse(availableMoves.contains(0));
        assertFalse(availableMoves.contains(4));
    }

    @Test
    void evaluateBoardStates() {
        game.symbol = 'X';
        assertEquals(Game.INF, game.evaluatePosition(new char[]{'X','X','X',' ',' ',' ',' ',' ',' '}, playerX));
        assertEquals(-Game.INF, game.evaluatePosition(new char[]{'X','X','X',' ',' ',' ',' ',' ',' '}, playerO));

        char[] drawBoard = {'X','O','X','X','O','O','O','X','X'};
        assertEquals(0, game.evaluatePosition(drawBoard, playerX));

        assertEquals(-1, game.evaluatePosition(new char[]{'X',' ',' ',' ',' ',' ',' ',' ',' '}, playerX));
    }

    @Test
    void testMinimaxFunctionality() {
        assertTrue(game.MiniMax(board, playerX) >= 1);
        char[] almostDone = {'X','O','X','O','X','O','O','X',' '};
        game.board = almostDone;
        assertEquals(9, game.MiniMax(almostDone, playerX));
    }

    @Test
    void minAndMaxValueTests() {
        char[] xWinBoard = {'X','X',' ','O','O',' ',' ',' ',' '};
        game.board = xWinBoard;
        game.symbol = 'X';
        assertEquals(-Game.INF, game.MinMove(xWinBoard, playerX));

        char[] oWinBoard = {'O','O',' ','X','X',' ',' ',' ',' '};
        game.board = oWinBoard;
        game.symbol = 'O';
        assertEquals(Game.INF, game.MaxMove(oWinBoard, playerO));
    }

    @Test
    void validateTicTacToeCellBehavior() {
        TicTacToeCell cell = new TicTacToeCell(0, 0, 0);
        assertEquals(' ', cell.getMarker());
        cell.setMarker("X");
        assertEquals('X', cell.getMarker());
        assertFalse(cell.isEnabled());
    }

    @Test
    void panelCellsAreInitialized() throws Exception {
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        assertEquals(9, cells.length);
        for (int i = 0; i < 9; i++) {
            assertEquals(i, cells[i].getNum());
        }
    }

    @Test
    void panelClickUpdatesCell() throws Exception {
        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);
        cells[0].doClick();
        assertEquals('X', cells[0].getMarker());
    }

    @Test
    void gameStateUpdatesOnMove() throws Exception {
        Field gameField = TicTacToePanel.class.getDeclaredField("game");
        gameField.setAccessible(true);
        Game g = (Game) gameField.get(panel);

        Field cellsField = TicTacToePanel.class.getDeclaredField("cells");
        cellsField.setAccessible(true);
        TicTacToeCell[] cells = (TicTacToeCell[]) cellsField.get(panel);

        cells[0].doClick();
        cells[1].doClick();

        assertEquals(State.PLAYING, g.state);
    }

    @Test
    void integrationTestPanelWithFrame() {
        JFrame frame = new JFrame();
        frame.add(panel);
        frame.setSize(200, 200);
        frame.setVisible(true);
        assertEquals(9, panel.getComponentCount());
        frame.dispose();
    }
}