package Layout_new.StationExecutor;

import JSON.EnumsAndParameters.*;
import Layout_new.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @see StationExecutorLogic
 */
public class SortingStirringExecutorLogic extends StationExecutorLogic {

    List<MachineCommandParamTriple> processListFirstPart, processListVariableMiddle, processListLastPart;

    Machine Conv11 = new Machine("1.1-Conv", CommandType.CONVEYOR,1);
    Machine Grip12 = new Machine("1.2-Grip", CommandType.GRIPPER, 1);
    Machine Conv13 = new Machine("1.3-Conv", CommandType.CONVEYOR, 1);
    Machine Sort14 = new Machine("1.4-Sort", CommandType.SORTING, 1);
    Machine Store15 = new Machine("1.5-Store", CommandType.WAREHOUSE, 9);
    Machine Vac16 = new Machine("1.6-Vac", CommandType.VACUUM, 1);
    Machine Indexed17 = new Machine("1.7-Indexed", CommandType.INDEXEDLINE, 1);
    Machine Grip18 = new Machine("1.8-Grip", CommandType.GRIPPER, 1);

    //TODO auslagern in config datei
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Config TODO postionen bestimmen
    Position posIOStationByGrip12 = new Position(1500,3300,0);
    Position posConv11ByGrip12 = new Position(1000,1900,70);
    Position posConv13ByGrip12 = new Position(1000, 0, 0);
    Position posSort14WhiteByVac16 = new Position(600,2800,100);
    Position posSort14BlueByVac16 = new Position(0, 0, 0);
    Position posSort14RedByVac16 = new Position(0, 0, 0);
    Position posConv13ByVac16 = new Position(400, 200, 0);
    Position posStore15ByVac16 = new Position(200, 1500, 800);
    Position posIndexed17ByVac16 = new Position(500, 2200, 1000);
    Position posGrip18OverloadByVac16 = new Position(1000, 2000, 40);
    Position posIndexed17ByGrip18 = new Position(1000, 1500, 0);
    Position posVac16OverloadByGrip18 = new Position(500, 3000, 60);


    PositionParameterThreeD posIOStationByGrip12Start = new PositionParameterThreeD(PositionMeaning.START, posIOStationByGrip12.getVertical(), posIOStationByGrip12.getRot(), posIOStationByGrip12.getHorizontal());
    PositionParameterThreeD posConv11ByGrip12End = new PositionParameterThreeD(PositionMeaning.END, posConv11ByGrip12.getVertical(), posConv11ByGrip12.getRot(), posConv11ByGrip12.getHorizontal());
    Parameter directionForward = new Parameter(Direction.FORWARD);
    Parameter colourWhite = new Parameter(Colour.WHITE);
    Parameter colourRed = new Parameter(Colour.RED);
    Parameter colourBlue = new Parameter(Colour.BLUE);
    PositionParameterThreeD posSort14WhiteByVac16Start = new PositionParameterThreeD(PositionMeaning.START, posSort14WhiteByVac16.getVertical(), posSort14WhiteByVac16.getRot(), posSort14WhiteByVac16.getHorizontal());
    PositionParameterThreeD posSort14BlueByVac16Start = new PositionParameterThreeD(PositionMeaning.START, posSort14BlueByVac16.getVertical(), posSort14BlueByVac16.getRot(), posSort14BlueByVac16.getHorizontal());
    PositionParameterThreeD posSort14RedByVac16Start = new PositionParameterThreeD(PositionMeaning.START, posSort14RedByVac16.getVertical(), posSort14RedByVac16.getRot(), posSort14RedByVac16.getHorizontal());
    PositionParameterThreeD posIndexed17ByVac16End = new PositionParameterThreeD(PositionMeaning.END, posIndexed17ByVac16.getVertical(), posIndexed17ByVac16.getRot(), posIndexed17ByVac16.getHorizontal());
    PositionParameterThreeD posIndexed17ByVac16Start = new PositionParameterThreeD(PositionMeaning.START, posIndexed17ByVac16.getVertical(), posIndexed17ByVac16.getRot(), posIndexed17ByVac16.getHorizontal());
    PositionParameterThreeD posStore15ByVac16Start = new PositionParameterThreeD(PositionMeaning.START, posStore15ByVac16.getVertical(), posStore15ByVac16.getRot(), posStore15ByVac16.getHorizontal());
    PositionParameterThreeD posStore15ByVac16End = new PositionParameterThreeD(PositionMeaning.END, posStore15ByVac16.getVertical(), posStore15ByVac16.getRot(), posStore15ByVac16.getHorizontal());
    PositionParameterThreeD posGrip18OverloadByVac16Start = new PositionParameterThreeD(PositionMeaning.START, posGrip18OverloadByVac16.getVertical(), posGrip18OverloadByVac16.getRot(), posGrip18OverloadByVac16.getHorizontal());
    PositionParameterThreeD posConv13ByVac16End = new PositionParameterThreeD(PositionMeaning.END, posConv13ByVac16.getVertical(), posConv13ByVac16.getRot(), posConv13ByVac16.getHorizontal());
    Parameter number3 = new Parameter(new NumberNatural(3));
    PositionParameterThreeD posIndexed17ByGrip18Start = new PositionParameterThreeD(PositionMeaning.START, posIndexed17ByGrip18.getVertical(), posIndexed17ByGrip18.getRot(), posIndexed17ByGrip18.getHorizontal());
    PositionParameterThreeD posVac16OverloadByGrip18End = new PositionParameterThreeD(PositionMeaning.END, posVac16OverloadByGrip18.getVertical(), posVac16OverloadByGrip18.getRot(), posVac16OverloadByGrip18.getHorizontal());
    Parameter directionBackward = new Parameter(Direction.BACKWARD);
    PositionParameterThreeD posConv13ByGrip12Start = new PositionParameterThreeD(PositionMeaning.START, posConv13ByGrip12.getVertical(), posConv13ByGrip12.getRot(), posConv13ByGrip12.getHorizontal());
    PositionParameterThreeD posIOStationByGrip12End = new PositionParameterThreeD(PositionMeaning.END, posIOStationByGrip12.getVertical(), posIOStationByGrip12.getRot(), posIOStationByGrip12.getHorizontal());

    public SortingStirringExecutorLogic(InputOutputStation IOStation){
        super(IOStation, RevPiNumber.CORE1);

        this.processListFirstPart = new LinkedList<>();
        this.processListVariableMiddle = new LinkedList<>();
        this.processListLastPart = new LinkedList<>();

        //ABLAUF processYoghurt
        //1 - move mit Robot 1 auf Förderband 1
        //2 - Transport mit Förderband 1
        //3 - Sortierung
        //4 - Transport mit Vac auf indexed line
        //5 - warehouse auslagern
        //6 - Vac legt yoghurt aus Warehouse auf behälter in indexed line
        //7 - wenn stirred: indexed line laufen lassen
        //8 - wenn stirred: robot 2 an übergabestation legen
        //9 - wenn stirred: vac hebt objekt von übergabestation auf Förderband 2
        //789 - wenn not stirred: vac hebt objekt von indexedLine auf Föderband 2
        //5 - Transport mit Förderband 2
        //6 - move mit Robot 1 auf IO Station
        //TODO ausprobieren ob paralleles setup von hinten stehenden Maschinen eine gute idee ist
        //Grip12
        List<Parameter> paramListGrip12Yoghurt = new LinkedList<>();
        paramListGrip12Yoghurt.add(new Parameter(posIOStationByGrip12Start));
        paramListGrip12Yoghurt.add(new Parameter(posConv11ByGrip12End));
        processListFirstPart.add(new MachineCommandParamTriple(Grip12, CommandNames.MOVE, paramListGrip12Yoghurt, false));
        //Conv11
        List<Parameter> paramListConv11Yoghurt = new LinkedList<>();
        paramListConv11Yoghurt.add(directionBackward);
        processListFirstPart.add(new MachineCommandParamTriple(Conv11, CommandNames.MOVE, paramListConv11Yoghurt, true));

        //Conv13
        List<Parameter> paramListConv13Yoghurt = new LinkedList<>();
        paramListConv13Yoghurt.add(directionBackward);
        processListLastPart.add(new MachineCommandParamTriple(Conv13, CommandNames.MOVELB, paramListConv13Yoghurt, false));
        //Grip12
        List<Parameter> paramListGrip12Yoghurt2 = new LinkedList<>();
        paramListGrip12Yoghurt2.add(new Parameter(posConv13ByGrip12Start));
        paramListGrip12Yoghurt2.add(new Parameter(posIOStationByGrip12End));
        processListLastPart.add(new MachineCommandParamTriple(Grip12, CommandNames.MOVE, paramListGrip12Yoghurt2, false));


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    /**
     * Generates a process list in relation to the parameters stirred and colour which come from the
     * input object (see run method)
     * @param stirred
     * @param colour
     * @return
     */
    private List<MachineCommandParamTriple> processListGen(boolean stirred, Colour colour){
        //Sort14
        List<Parameter> paramListSort14Yoghurt = new LinkedList<>();
        paramListSort14Yoghurt.add(new Parameter(colour));
        processListVariableMiddle.add(new MachineCommandParamTriple(Sort14, CommandNames.EJECT, paramListSort14Yoghurt, false));
        //Vac16
        List<Parameter> paramListVac16Yoghurt = new LinkedList<>();
        if(colour == Colour.WHITE){
            paramListVac16Yoghurt.add(new Parameter(posSort14WhiteByVac16Start));
        } else if (colour == Colour.BLUE) {
            paramListVac16Yoghurt.add(new Parameter(posSort14BlueByVac16Start));
        } else if (colour == Colour.RED) {
            paramListVac16Yoghurt.add(new Parameter(posSort14RedByVac16Start));
        }
        paramListVac16Yoghurt.add(new Parameter(posIndexed17ByVac16End));
        processListVariableMiddle.add(new MachineCommandParamTriple(Vac16, CommandNames.MOVE, paramListVac16Yoghurt, false)); //TODO maybe true
        //Store15
        List<Parameter> paramListStore15Yoghurt = new LinkedList<>();
        paramListStore15Yoghurt.add(new Parameter(BoxNumber.BOX1));
        processListVariableMiddle.add(new MachineCommandParamTriple(Store15, CommandNames.GET, paramListStore15Yoghurt, false));
        //Vac16
        List<Parameter> paramListVac16Yoghurt2 = new LinkedList<>();
        paramListVac16Yoghurt2.add(new Parameter(posStore15ByVac16Start));
        paramListVac16Yoghurt2.add(new Parameter(posIndexed17ByVac16End));
        processListVariableMiddle.add(new MachineCommandParamTriple(Vac16, CommandNames.MOVE, paramListVac16Yoghurt2, false));
        //stirred?
        if(stirred) {
            //Indexed17
            List<Parameter> paramListIndexed17Yoghurt = new LinkedList<>();
            paramListIndexed17Yoghurt.add(number3);
            processListVariableMiddle.add(new MachineCommandParamTriple(Indexed17, CommandNames.STIRR, paramListIndexed17Yoghurt, false));
            //Grip18
            List<Parameter> paramListGrip18Yoghurt = new LinkedList<>();
            paramListGrip18Yoghurt.add(new Parameter(posIndexed17ByGrip18Start));
            paramListGrip18Yoghurt.add(new Parameter(posVac16OverloadByGrip18End));
            processListVariableMiddle.add(new MachineCommandParamTriple(Grip18, CommandNames.MOVE, paramListGrip18Yoghurt, false));
            //Vac16
            List<Parameter> paramListVac16Yoghurt3 = new LinkedList<>();
            paramListVac16Yoghurt3.add(new Parameter(posGrip18OverloadByVac16Start));
            paramListVac16Yoghurt3.add(new Parameter(posConv13ByVac16End));
            processListVariableMiddle.add(new MachineCommandParamTriple(Vac16, CommandNames.MOVE, paramListVac16Yoghurt3, false));
        } else {
            //Vac16
            List<Parameter> paramListVac16Yoghurt4 = new LinkedList<>();
            paramListVac16Yoghurt4.add(new Parameter(posIndexed17ByVac16Start));
            paramListVac16Yoghurt4.add(new Parameter(posConv13ByVac16End));
            processListVariableMiddle.add(new MachineCommandParamTriple(Vac16, CommandNames.MOVE, paramListVac16Yoghurt4, false));
        }
        processListFirstPart.addAll(processListVariableMiddle);
        processListFirstPart.addAll(processListLastPart);
        return processListFirstPart;
    }

    //TODO delete, once run method is used in higher tier factory management class
    public static void main(String[] args) {
        InputOutputStation ioStation = new InputOutputStation(1);
        SortingStirringExecutorLogic stirr = new SortingStirringExecutorLogic(ioStation);
        try {
            ioStation.putInputObject(new Yoghurt());
        } catch (IllegalActionException e) {
            throw new RuntimeException(e);
        }
        while(true){
            try {
                Processable inputObject = null;
                List<MachineCommandParamTriple> processList = null;
                if(stirr.getStepChainExecutorFinished()) {
                    try {
                        inputObject = stirr.getInputObject();
                    } catch (IllegalActionException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputObject instanceof Yoghurt) {
                        //TODO info aus input object bekommen
                        processList = stirr.processListGen(true, Colour.WHITE);
                    }
                }
                //TODO input oder processList null abfangen -> exception
                stirr.process(inputObject, processList);
            } catch (ExecutorException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * copy of main without setup -- used in demo
     */
    public void run(){
        while(true){
            try {
                Processable inputObject = null;
                List<MachineCommandParamTriple> processList = null;
                if(this.getStepChainExecutorFinished()) {
                    try {
                        inputObject = this.getInputObject();
                    } catch (IllegalActionException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputObject instanceof Yoghurt) {
                        //TODO info aus input object bekommen
                        processList = this.processListGen(true, Colour.WHITE);
                    }
                }
                //TODO input oder processList null abfangen -> exception
                this.process(inputObject, processList);
            } catch (ExecutorException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
