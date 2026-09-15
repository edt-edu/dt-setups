package Layout_new.StationExecutor;

import JSON.EnumsAndParameters.*;
import Layout_new.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @see StationExecutorLogic
 */
public class FreezingExecutorLogic extends StationExecutorLogic{

    List<MachineCommandParamTriple> processListRaw, processListYoghurt;

    public FreezingExecutorLogic(InputOutputStation IOStation) {
        super(IOStation, RevPiNumber.CORE2);

        this.processListYoghurt = new LinkedList<>();
        this.processListRaw = new LinkedList<>();

        Machine Grip21 = new Machine("2.1-Grip", CommandType.GRIPPER, 1);
        Machine Grip22 = new Machine("2.2-Grip", CommandType.GRIPPER, 1);
        Machine Conv23 = new Machine("2.3-Conv", CommandType.CONVEYOR, 1);
        Machine Conv24 = new Machine("2.4-Conv", CommandType.CONVEYOR, 1);
        Machine Vac25 = new Machine("2.5-Vac", CommandType.VACUUM, 1);
        Machine Freeze26 = new Machine("2.6-Freeze", CommandType.MULTIPROCESSING, 1);

        //TODO auslagern in config datei
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Config TODO postionen bestimmen
        Position posIOStationByGrip21 = new Position(2550,3000,10);
        Position posConv23ByGrip21 = new Position(2000,1600,60);
        Position posConv23ByVac25 = new Position(500,600,100);
        Position posFreezeByVac25 = new Position(500,1300,200);
        Position posConv24ByGrip22 = new Position(2000,500,20);
        Position posIOStationByGrip22 = new Position(2000,2000,0);


        PositionParameterThreeD posIOStationByGrip21Start = new PositionParameterThreeD(PositionMeaning.START, posIOStationByGrip21.getVertical(), posIOStationByGrip21.getRot(), posIOStationByGrip21.getHorizontal());
        PositionParameterThreeD posConv23ByGrip21End = new PositionParameterThreeD(PositionMeaning.END, posConv23ByGrip21.getVertical(),posConv23ByGrip21.getRot(),posConv23ByGrip21.getHorizontal());
        Parameter directionForward = new Parameter(Direction.FORWARD);
        PositionParameterThreeD posConv23ByVac25Start = new PositionParameterThreeD(PositionMeaning.START, posConv23ByVac25.getVertical(), posConv23ByVac25.getRot(), posConv23ByVac25.getHorizontal());
        PositionParameterThreeD posFreezeByVac25End = new PositionParameterThreeD(PositionMeaning.END, posFreezeByVac25.getVertical(), posFreezeByVac25.getRot(), posFreezeByVac25.getHorizontal());
        Parameter number3 = new Parameter(new NumberNatural(3));
        Parameter directionBackward = new Parameter(Direction.BACKWARD);
        PositionParameterThreeD posConv24ByGrip22Start = new PositionParameterThreeD(PositionMeaning.START, posConv24ByGrip22.getVertical(), posConv24ByGrip22.getRot(), posConv24ByGrip22.getHorizontal());
        PositionParameterThreeD posIOStationByGrip22End = new PositionParameterThreeD(PositionMeaning.END, posIOStationByGrip22.getVertical(), posIOStationByGrip22.getRot(), posIOStationByGrip22.getHorizontal());


        //ABLAUF processYoghurt
        //1 - move mit Robot 1 auf Förderband 1
        //2 - Transport mit Förderband 1
        //3 - Vac platziert auf freeze - freeze muss mit korrektem setup dastehen
        //4 - freeze
        //5 - Transport mit Förderband 2
        //6 - move mit Robot 2 auf IO Station
        //TODO ausprobieren ob paralleles setup von hinten stehenden Maschinen eine gute idee ist
        //Freeze26
        List<Parameter> paramListFreezeSetup = new LinkedList<>();
        processListYoghurt.add(new MachineCommandParamTriple(Grip22, CommandNames.SETUP, paramListFreezeSetup, true));
        processListYoghurt.add(new MachineCommandParamTriple(Vac25, CommandNames.SETUP, paramListFreezeSetup, true));
        //processListYoghurt.add(new MachineCommandParamTriple(Freeze26, CommandNames.SETUP, paramListFreezeSetup, true));
        //Grip21
        List<Parameter> paramListGrip21Yoghurt = new LinkedList<>();
        paramListGrip21Yoghurt.add(new Parameter(posIOStationByGrip21Start));
        paramListGrip21Yoghurt.add(new Parameter(posConv23ByGrip21End));
        processListYoghurt.add(new MachineCommandParamTriple(Grip21, CommandNames.MOVE, paramListGrip21Yoghurt, false));
        //Conv23
        List<Parameter> paramListConv23Yoghurt = new LinkedList<>();
        paramListConv23Yoghurt.add(directionForward);
        processListYoghurt.add(new MachineCommandParamTriple(Conv23, CommandNames.MOVELB, paramListConv23Yoghurt, false));
        //Vac25
        List<Parameter> paramListVac25Yoghurt = new LinkedList<>();
        paramListVac25Yoghurt.add(new Parameter(posConv23ByVac25Start));
        paramListVac25Yoghurt.add(new Parameter(posFreezeByVac25End));
        processListYoghurt.add(new MachineCommandParamTriple(Vac25, CommandNames.MOVE, paramListVac25Yoghurt, false));
        //Freeze26
        List<Parameter> paramListFreeze26Yoghurt = new LinkedList<>();
        paramListFreeze26Yoghurt.add(number3);
        processListYoghurt.add(new MachineCommandParamTriple(Freeze26, CommandNames.FREEZE, paramListFreeze26Yoghurt, true));
        //Conv24
        List<Parameter> paramListConv24Yoghurt = new LinkedList<>();
        paramListConv24Yoghurt.add(directionBackward);
        processListYoghurt.add(new MachineCommandParamTriple(Conv24, CommandNames.MOVELB, paramListConv24Yoghurt, false));
        //Grip22
        List<Parameter> paramListGrip22Yoghurt = new LinkedList<>();
        paramListGrip22Yoghurt.add(new Parameter(posConv24ByGrip22Start));
        paramListGrip22Yoghurt.add(new Parameter(posIOStationByGrip22End));
        processListYoghurt.add(new MachineCommandParamTriple(Grip22, CommandNames.MOVE, paramListGrip22Yoghurt, false));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    //TODO delete, once run method is used in higher tier factory management class
    public static void main(String[] args) throws InterruptedException {
        InputOutputStation ioStation = new InputOutputStation(1);
        FreezingExecutorLogic freeze = new FreezingExecutorLogic(ioStation);
        try {
            ioStation.putInputObject(new Yoghurt());
        } catch (IllegalActionException e) {
            throw new RuntimeException(e);
        }
        TimeUnit.SECONDS.sleep(5);
        while(true){
            try {
                Processable inputObject = null;
                List<MachineCommandParamTriple> processList = null;
                if(freeze.getStepChainExecutorFinished()) {
                    try {
                        inputObject = freeze.getInputObject();
                    } catch (IllegalActionException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputObject instanceof Yoghurt) {
                        processList = freeze.processListYoghurt;
                    }
                }
                //TODO input oder processList null abfangen -> exception
                freeze.process(inputObject, processList);
            } catch (ExecutorException e) {
                throw new RuntimeException(e);
            }
        }
    }

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
                        processList = this.processListYoghurt;
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
