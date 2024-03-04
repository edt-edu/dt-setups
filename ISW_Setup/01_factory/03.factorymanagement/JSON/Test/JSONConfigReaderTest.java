package JSON.Test;

import JSON.CommandsANDStatusrequests.*;
import JSON.EnumsAndParameters.CommandType;
import JSON.EnumsAndParameters.RequestedParameter;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class JSONConfigReaderTest {

    JSONConfigReader parser;

    @Before
    public void setup(){
        parser = new JSONConfigReader();
    }

    @Test
    public void testConfigReadingExceptions() throws IOException {
        //test that the actual config file doesn't throw exceptions
        System.out.println(parser.readCommandConfig("03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineCommands.json"));
        System.out.println(parser.readStatusRequestConfig("03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineStatusRequests.json"));
        //test that duplicate machines throw exceptions
        System.out.println("Test duplicate Configs for one machine");
        Exception e = assertThrows(ValueInstantiationException.class, () -> parser.readCommandConfig("03.factorymanagement/JSON/Test/testConf01.json"));
        assertTrue(e.getMessage().contains("there are two configs for the same machine"));
        //test that duplicate methods in  a machine throw exceptions
        System.out.println("Test duplicate commands in one machine");
        e = assertThrows(ValueInstantiationException.class, () -> parser.readCommandConfig("03.factorymanagement/JSON/Test/testConf02.json"));
        assertTrue(e.getMessage().contains("there are two configs for the same command within one machine"));
        //test that if parameter list differ in length an exception is thrown
        System.out.println("Test parameter list differ in length");
        e = assertThrows(ValueInstantiationException.class, () -> parser.readCommandConfig("03.factorymanagement/JSON/Test/testConf03.json"));
        assertTrue(e.getMessage().contains("number of parameters differs from number of types given"));
        //test what happens if config for statusRequests is read
        //use of a jackson builtin exception without further checking is enough
        System.out.println("Test wrong config file of other type is read");
        e = assertThrows(ValueInstantiationException.class, () -> parser.readCommandConfig("03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineStatusRequests.json"));
        assertTrue(e.getMessage().contains("Cannot construct instance"));
    }

    @Test
    public void testConfigReadingRequestsWithDuplicates() throws IOException {
        //AllMachineStatusRequestConfigs a = parser.readStatusRequestConfig("03.factorymanagement/JSON/Test/testConf04.json");
        Exception e = assertThrows(ValueInstantiationException.class, () -> parser.readStatusRequestConfig("03.factorymanagement/JSON/Test/testConf04.json"));
        assertTrue(e.getMessage().contains("there are duplicate requests within one machine"));
        System.out.println(e.getMessage());
    }

    @Test
    public void testConfigReadingCommandsGoodCases() throws IOException {
        AllMachineCommandConfigs allConfigs = parser.readCommandConfig("03.factorymanagement/JSON/Test/testConfGood.json");
        assertEquals(8, allConfigs.getAllConfigs().size());
        //TODO test in depth
        for(AnyMachineCommandConfig oneConfig: allConfigs.getAllConfigs()){
            if(oneConfig.getType() == CommandType.PUNCHING || oneConfig.getType() == CommandType.MULTIPROCESSING || oneConfig.getType() == CommandType.SORTING || oneConfig.getType() == CommandType.INDEXEDLINE){
                //enthält 2 Commands
                assertEquals(2, oneConfig.getAllCommands().size());
            } else if(oneConfig.getType() == CommandType.CONVEYOR){
                //enthält 3 Commands
                assertEquals(3, oneConfig.getAllCommands().size());
            } else if(oneConfig.getType() == CommandType.GRIPPER || oneConfig.getType() == CommandType.VACUUM || oneConfig.getType() == CommandType.WAREHOUSE){
                //enthält 6 Commands
                assertEquals(6, oneConfig.getAllCommands().size());
            }
        }
    }
}
