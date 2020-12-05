package com.codenjoy.dojo.bot;

import com.codenjoy.dojo.services.Command;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.Rotation;
import com.codenjoy.dojo.tetris.client.Board;
import com.codenjoy.dojo.tetris.client.GlassBoard;
import com.codenjoy.dojo.tetris.model.Elements;

import java.util.*;

public class Bot {
    private GlassBoard glassBoard;
    private List<Point> busyPoints;
    private Elements currentElement;
    private Point currentPoint;
    private Point[] currentFigure;
    private final int SIZE = 17;
    private int figureWidth;
    private ArrayList<State> listPoints;
    private Position position = Position.UP;
    private int numberPossiblePosition = 0;

    private void initialize(Board board) {
        this.glassBoard = board.getGlass();
        this.busyPoints = board.getGlass().getFigures();
        this.currentElement = board.getCurrentFigureType();
        this.currentPoint = board.getCurrentFigurePoint();
        this.currentFigure = board.predictCurrentFigurePoints();
        this.figureWidth = getRightPointOfFigure() - getLeftPointOfFigure();
        this.listPoints = new ArrayList();
        busyPoints.removeAll(Arrays.asList(currentFigure));
    }

    public List<Command> move(Board board) {
        List<Command> result = null;
        initialize(board);
        System.out.println(board.getGlass().toString());
        numberPossiblePosition = SIZE - figureWidth;
        System.out.println("Ширина фигуры: " + (figureWidth + 1));
        System.out.println("Количество вариантов постановки хигуры: " + numberPossiblePosition);
        switch (currentElement) {
            case YELLOW:

                checkingOptions(numberPossiblePosition);
                listPoints.sort(Comparator.comparingInt(State::getPoint));
                System.out.println(listPoints);
                result = createMove(chooseBestMove());
                break;

            case BLUE:
            case RED:
            case GREEN:

                for (int external = 0; external < 2; external++) {
                    checkingOptions(numberPossiblePosition);
                    turnFigure(board,external);
                }
                listPoints.sort(Comparator.comparingInt(State::getPoint));
                System.out.println(listPoints);
                result = createMove(chooseBestMove());
                break;

            case ORANGE:
            case CYAN:
            case PURPLE:
                for (int external = 0; external < 3; external++) {
                    checkingOptions(numberPossiblePosition);
                    turnFigure(board,external);
                }
                listPoints.sort(Comparator.comparingInt(State::getPoint));
                System.out.println(listPoints);
                result = createMove(chooseBestMove());
                break;
        }
        return result;
    }

    private State chooseBestMove() {
        return listPoints.get(0);
    }

    private void checkingOptions(int numberPossiblePosition) {
        for (int internal = 0; internal <= numberPossiblePosition; internal++) {
            moveFigureToEdge(internal);
            while (getBottomPointOfFigure() > 0 && checkPosition()) {
                moveFigureBottom();
            }
            listPoints.add(new State(position, countingPoints(), internal));
        }
    }

    private void turnFigure(Board board, int direction) {
        currentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_90);
        figureWidth = getRightPointOfFigure() - getLeftPointOfFigure();
        numberPossiblePosition = SIZE - figureWidth;
        switch (direction) {
            case 0: position = Position.RIGHT; break;
            case 1: position = Position.DOWN; break;
            case 2: position = Position.LEFT; break;
        }
    }

    private List<Command> createMove(State state) {
        List<Command> result = new ArrayList<>();
        Position position = state.getPosition();
        int place = state.getPlace();
        int anchorPlace = currentPoint.getX();
        switch (position) {
            case RIGHT: result.add(Command.ROTATE_CLOCKWISE_90); break;
            case DOWN: result.add(Command.ROTATE_CLOCKWISE_180); break;
            case LEFT: result.add(Command.ROTATE_CLOCKWISE_270); break;
        }
        if (place < anchorPlace) {
            for (int i = 0; i < anchorPlace - place; i++) {
                result.add(Command.LEFT);
            }
        } else {
            for (int i = 0; i < place - anchorPlace; i++) {
                result.add(Command.RIGHT);
            }
        }
        result.add(Command.DOWN);
        return result;
    }

    private int countingPoints() {
        int numberEmptyPoint = countingEmptyPoint(getLeftPointOfFigure(), getTopPointOfFigure());
        int towerHeight = getTopPointOfFigure();
        System.out.println("Количество пустых ячеек: " + numberEmptyPoint);
        return numberEmptyPoint + towerHeight;
    }

    private int countingEmptyPoint(int leftEdge, int topEdge) {
        int numberEmptyPoint = 0;
        for (int row = leftEdge; row < leftEdge + figureWidth; row++) {
            for (int column = topEdge; column >= 0; column--) {
                System.out.println("Проверяем ячейку: " + row + " " + column);
                if (!checkYourPoint(row,column) && glassBoard.isFree(row,column)) {
                    numberEmptyPoint++;
                }
            }
        }
        return numberEmptyPoint;
    }

    private boolean checkYourPoint(int row, int column) {
        for (Point point:currentFigure) {
            if (point.itsMe(row, column)) return true;
        }
        return false;
    }

    private boolean checkPosition() {
        for (Point point : currentFigure) {
            if (busyPoints.contains(point.shiftBottom(1))) {
                return false;
            }
        }
        return true;
    }

    private void moveFigureBottom(){
        for (int i = 0; i < currentFigure.length; i++) {
            currentFigure[i] = currentFigure[i].shiftBottom(1);
        }
    }

    private void moveFigureToEdge(int delta){
        int leftEdgeDistance = getLeftPointOfFigure();
        int topEdgeDistance = SIZE - getTopPointOfFigure();
        for (Point point : currentFigure) {
            point.move(point.getX() - leftEdgeDistance + delta, point.getY() + topEdgeDistance);
        }
    }

    private int getTopPointOfFigure() {
        int topPoint = currentFigure[0].getY();
        for (int i = 1; i < currentFigure.length; i++) {
            int temp = currentFigure[i].getY();
            if (temp > topPoint) {
                topPoint = temp;
            }
        }
        return topPoint;
    }

    private int getBottomPointOfFigure() {
        int bottomPoint = currentFigure[0].getY();
        for (int i = 1; i < currentFigure.length; i++) {
            int temp = currentFigure[i].getY();
            if (temp < bottomPoint) {
                bottomPoint = temp;
            }
        }
        return bottomPoint;
    }

    private int getLeftPointOfFigure() {
        int leftPoint = currentFigure[0].getX();
        for (int i = 1; i < currentFigure.length; i++) {
            int temp = currentFigure[i].getX();
            if (temp < leftPoint) {
                leftPoint = temp;
            }
        }
        return leftPoint;
    }

    private int getRightPointOfFigure() {
        int rightPoint = currentFigure[0].getX();
        for (int i = 1; i < currentFigure.length; i++) {
            int temp = currentFigure[i].getX();
            if (temp > rightPoint) {
                rightPoint = temp;
            }
        }
        return rightPoint;
    }

//    private void show() {
//        StringBuilder builder = new StringBuilder();
//        for (Point point : currentFigure) {
//            builder.append("{x=")
//                    .append(point.getX())
//                    .append(" y=")
//                    .append(point.getY())
//                    .append("}\n");
//        }
//        System.out.println(builder.toString());
//    }
}