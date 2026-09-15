package Layout_new.StationExecutor;

import JSON.EnumsAndParameters.*;
import Layout_new.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @see StationExecutorLogic
 */
public class ToppingExecutorLogic extends StationExecutorLogic {

    List<MachineCommandParamTriple> processListRaw, processListYoghurt;

    public ToppingExecutorLogic(InputOutputStation IOStation) {
        super(IOStation, RevPiNumber.CORE4);

        this.processListYoghurt = new LinkedList<>();
        this.processListRaw = new LinkedList<>();

        Machine Grip41 = new Machine("4.1-Grip", CommandType.GRIPPER, 1);
        Machine Conv42 = new Machine("4.2-Conv", CommandType.CONVEYOR, 1);
        Machine Conv43 = new Machine("4.3-Conv", CommandType.CONVEYOR, 1);
        Machine Vac44 = new Machine("4.4-Vac", CommandType.VACUUM, 1);
        Machine Store45 = new Machine("4.5-Store", CommandType.WAREHOUSE, 9);

        //TODO auslagern in config datei
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Config
        Position posIOStationByGrip41 = new Position(2850,2700,10);
        Position posConv2byGrip41 = new Position(2050,1800,75);
        Position posConv43x12ByVac44 = new Position(950,1010, 1750);
        Position posStoreIOByVac44 = new Position(200,615,1550);


        PositionParameterThreeD posIOStationByGrip41Start = new PositionParameterThreeD(PositionMeaning.START, posIOStationByGrip41.getVertical(),posIOStationByGrip41.getRot(),posIOStationByGrip41.getHorizontal());
        PositionParameterThreeD posConv2byGrip41End = new PositionParameterThreeD(PositionMeaning.END, posConv2byGrip41.getVertical(),posConv2byGrip41.getRot(),posConv2byGrip41.getHorizontal());
        Parameter directionForward = new Parameter(Direction.FORWARD);
        PositionParameterThreeD posConv43x12ByVac44Start = new PositionParameterThreeD(PositionMeaning.START, posConv43x12ByVac44.getVertical(),posConv43x12ByVac44.getRot(),posConv43x12ByVac44.getHorizontal());
        PositionParameterThreeD posStoreIOByVac44End = new PositionParameterThreeD(PositionMeaning.END, posStoreIOByVac44.getVertical(),posStoreIOByVac44.getRot(),posStoreIOByVac44.getHorizontal());
        PositionParameterThreeD posStoreIOByVac44Start = new PositionParameterThreeD(PositionMeaning.START, posStoreIOByVac44.getVertical(),posStoreIOByVac44.getRot(),posStoreIOByVac44.getHorizontal());
        PositionParameterThreeD posConv43x12ByVac44End = new PositionParameterThreeD(PositionMeaning.END, posConv43x12ByVac44.getVertical(),posConv43x12ByVac44.getRot(),posConv43x12ByVac44.getHorizontal());
        Parameter directionBackward = new Parameter(Direction.BACKWARD);
        PositionParameterThreeD posConv2byGrip41Start = new PositionParameterThreeD(PositionMeaning.START, posConv2byGrip41.getVertical(),posConv2byGrip41.getRot(),posConv2byGrip41.getHorizontal());
        PositionParameterThreeD posIOStationByGrip41End = new PositionParameterThreeD(PositionMeaning.END, posIOStationByGrip41.getVertical(),posIOStationByGrip41.getRot(),posIOStationByGrip41.getHorizontal());

        //ABLAUF processRaw
        //1 - move mit Robot auf Förderband 1
        //2 - Transport von Förderband 1 und 2
        //3 - move mit Vacuum Gripper in Lagereingang
        //4 - Einlagerungsprozess im Warehouse
        //TODO ausprobieren ob paralleles setup von hinten stehenden Maschinen eine gute idee ist
        List<Parameter> paramListEmpty = new LinkedList<>();
        //Setup 44 u 45
        //processListRaw.add(new MachineCommandParamTriple(Vac44, CommandNames.SETUP, paramListEmpty, true));
        //processListRaw.add(new MachineCommandParamTriple(Store45, CommandNames.SETUP, paramListEmpty, true));

        //Grip41
        List<Parameter> paramListGrip41Raw = new LinkedList<>();
        paramListGrip41Raw.add(new Parameter(posIOStationByGrip41Start));
        paramListGrip41Raw.add(new Parameter(posConv2byGrip41End));
        processListRaw.add(new MachineCommandParamTriple(Grip41, CommandNames.MOVE, paramListGrip41Raw, false));
        //Conv42
        List<Parameter> paramListConv42Raw = new LinkedList<>();
        paramListConv42Raw.add(directionBackward);
        processListRaw.add(new MachineCommandParamTriple(Conv42, CommandNames.MOVE, paramListConv42Raw, true));
        //Conv43
        List<Parameter> paramListConv43Raw = new LinkedList<>();
        paramListConv43Raw.add(directionBackward);
        paramListConv43Raw.add(new Parameter(new NumberNatural(9)));
        processListRaw.add(new MachineCommandParamTriple(Conv43, CommandNames.GOTOCONFIG, paramListConv43Raw, false));
        //Vac44
        List<Parameter> paramListVac44Raw = new LinkedList<>();
        paramListVac44Raw.add(new Parameter(posConv43x12ByVac44Start));
        paramListVac44Raw.add(new Parameter(posStoreIOByVac44End));
        processListRaw.add(new MachineCommandParamTriple(Vac44, CommandNames.MOVE, paramListVac44Raw, false));
        //Store45
        List<Parameter> paramListStore45Raw = new LinkedList<>();
        paramListStore45Raw.add(new Parameter(BoxNumber.BOX1));
        processListRaw.add(new MachineCommandParamTriple(Store45, CommandNames.STORE, paramListStore45Raw, false));

        //ABLAUF processYoghurt
        //1 - move mit Robot auf Förderband 1
        //2 - Transport von Förderband 1 und 2
        //3 - Warehouse Auslagerungsprozess
        //4 - Vacuum Gripper move zur Toppingsplatzierung
        //5 - Transport von Förderband 1 und 2
        //6 - move mit Robot auf IO Station
        //TODO ausprobieren ob paralleles setup von hinten stehenden Maschinen eine gute idee ist
        //Setup 44 u 45
        //processListYoghurt.add(new MachineCommandParamTriple(Vac44, CommandNames.SETUP, paramListEmpty, true));
        //processListYoghurt.add(new MachineCommandParamTriple(Store45, CommandNames.SETUP, paramListEmpty, true));

        //Grip41
        List<Parameter> paramListGrip41Yoghurt = new LinkedList<>();
        paramListGrip41Yoghurt.add(new Parameter(posIOStationByGrip41Start));
        paramListGrip41Yoghurt.add(new Parameter(posConv2byGrip41End));
        processListYoghurt.add(new MachineCommandParamTriple(Grip41, CommandNames.MOVE, paramListGrip41Yoghurt, false));
        //Conv42
        List<Parameter> paramListConv42Yoghurt = new LinkedList<>();
        paramListConv42Yoghurt.add(directionBackward);
        processListYoghurt.add(new MachineCommandParamTriple(Conv42, CommandNames.MOVE, paramListConv42Yoghurt, true));
        //Conv43
        List<Parameter> paramListConv43Yoghurt = new LinkedList<>();
        paramListConv43Yoghurt.add(directionBackward);
        paramListConv43Yoghurt.add(new Parameter(new NumberNatural(9)));
        processListYoghurt.add(new MachineCommandParamTriple(Conv43, CommandNames.GOTOCONFIG, paramListConv43Yoghurt, false));
        //Store45
        List<Parameter> paramListStore45Yoghurt = new LinkedList<>();
        paramListStore45Yoghurt.add(new Parameter(BoxNumber.BOX1));
        processListYoghurt.add(new MachineCommandParamTriple(Store45, CommandNames.GET, paramListStore45Yoghurt, false));
        //Vac44
        List<Parameter> paramListVac44Yoghurt = new LinkedList<>();
        paramListVac44Yoghurt.add(new Parameter(posStoreIOByVac44Start));
        paramListVac44Yoghurt.add(new Parameter(posConv43x12ByVac44End));
        processListYoghurt.add(new MachineCommandParamTriple(Vac44, CommandNames.MOVE, paramListVac44Yoghurt, false));
        //Conv43
        List<Parameter> paramListConv43Yoghurt2 = new LinkedList<>();
        paramListConv43Yoghurt2.add(directionForward);
        processListYoghurt.add(new MachineCommandParamTriple(Conv43, CommandNames.MOVE, paramListConv43Yoghurt2, true));
        //Conv42
        List<Parameter> paramListConv42Yoghurt2 = new LinkedList<>();
        paramListConv42Yoghurt2.add(directionForward);
        paramListConv42Yoghurt2.add(new Parameter(new NumberNatural(8)));
        processListYoghurt.add(new MachineCommandParamTriple(Conv42, CommandNames.GOTOCONFIG, paramListConv42Yoghurt2, false));
        //Grip41
        List<Parameter> paramListGrip41Yoghurt2 = new LinkedList<>();
        paramListGrip41Yoghurt2.add(new Parameter(posConv2byGrip41Start));
        paramListGrip41Yoghurt2.add(new Parameter(posIOStationByGrip41End));
        //--works as a command for vac44 - cant be a logic error within moving machine class
        //PROBLEM : JSON mit id1 und 8 ansonsten exakt identisch, nicht gewollt, bewegungsrichtung und damit start end soll vertauscht sein - wo ist der fehler in dieser Config???
        processListYoghurt.add(new MachineCommandParamTriple(Grip41, CommandNames.MOVE, paramListGrip41Yoghurt2, false));


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }


    //TODO delete, once run method is used in higher tier factory management class
    public static void main(String[] args) {
        InputOutputStation ioStation = new InputOutputStation(1);
        ToppingExecutorLogic tel = new ToppingExecutorLogic(ioStation);
        try {
            //ioStation.putInputObject(new RawMaterial());
            ioStation.putInputObject(new Yoghurt());
        } catch (IllegalActionException e) {
            throw new RuntimeException(e);
        }
        while(true){
            try {
                Processable inputObject = null;
                List<MachineCommandParamTriple> processList = null;
                if(tel.getStepChainExecutorFinished()) {
                    try {
                        inputObject = tel.getInputObject();
                    } catch (IllegalActionException e) {
                        throw new RuntimeException(e);
                    }
                    if (inputObject instanceof RawMaterial) {
                        processList = tel.processListRaw;

                    } else if (inputObject instanceof Yoghurt) {
                        processList = tel.processListYoghurt;
                    }
                }
                //TODO input oder processList null abfangen -> exception
                tel.process(inputObject, processList);
            } catch (ExecutorException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void run() {
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
                    if (inputObject instanceof RawMaterial) {
                        processList = this.processListRaw;

                    } else if (inputObject instanceof Yoghurt) {
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
