package Layout_new.StationExecutor;

import JSON.EnumsAndParameters.*;
import Layout_new.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @see StationExecutorLogic
 *
 * Demo:
 * 1. Connect this pc to the fischertechnik router
 * 2. Check its ip(maybe 192.168.0.2)
 * 3. Start the mqtt broker: In C:\Program Files\mosquitto: .\mosquitto.exe -v -c .\mosquitto.conf
 * 4. Log into pi for island 2(192.168.1.120)
 * 5. Adjust ~/demo_27_05_2024/Python/RevPiMain2.py to contain the ip from step 2
 * 6. Run the main method of this file on this pc
 * 7. Run python3 RevPiMain2.py on the pi: MQTT_ENABLED=true MQTT_HOST=192.168.1.102 python3 RevPiMain2.py
 */
public class TurtlebotAndVacuumGripperDemo extends StationExecutorLogic{

    List<MachineCommandParamTriple> processListRaw, processListYoghurt;

    public TurtlebotAndVacuumGripperDemo(InputOutputStation IOStation) {
        super(IOStation, RevPiNumber.CORE2);

        this.processListYoghurt = new LinkedList<>();
        this.processListRaw = new LinkedList<>();

        Machine Vac25 = new Machine("2.5-Vac", CommandType.VACUUM, 1);

        //TODO auslagern in config datei
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Config TODO postionen bestimmen
        Position posConv23ByVac25 = new Position(1000,1800,2000);
        Position posFreezeByVac25 = new Position(1200,900,200);

        PositionParameterThreeD posConv23ByVac25Start = new PositionParameterThreeD(PositionMeaning.START, posConv23ByVac25.getVertical(), posConv23ByVac25.getRot(), posConv23ByVac25.getHorizontal());
        PositionParameterThreeD posFreezeByVac25End = new PositionParameterThreeD(PositionMeaning.END, posFreezeByVac25.getVertical(), posFreezeByVac25.getRot(), posFreezeByVac25.getHorizontal());


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
        processListYoghurt.add(new MachineCommandParamTriple(Vac25, CommandNames.SETUP, paramListFreezeSetup, true));
        //Vac25
        List<Parameter> paramListVac25Yoghurt = new LinkedList<>();
        paramListVac25Yoghurt.add(new Parameter(posConv23ByVac25Start));
        paramListVac25Yoghurt.add(new Parameter(posFreezeByVac25End));
        processListYoghurt.add(new MachineCommandParamTriple(Vac25, CommandNames.MOVE, paramListVac25Yoghurt, false));
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }


    public static void main(String[] args) throws InterruptedException {
        InputOutputStation ioStation = new InputOutputStation(1);
        TurtlebotAndVacuumGripperDemo freeze = new TurtlebotAndVacuumGripperDemo(ioStation);
        try {
            ioStation.putInputObject(new Yoghurt());
        } catch (IllegalActionException e) {
            throw new RuntimeException(e);
        }
        TimeUnit.SECONDS.sleep(2);
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
