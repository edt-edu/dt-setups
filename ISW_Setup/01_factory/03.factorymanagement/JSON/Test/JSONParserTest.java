package JSON.Test;

import JSON.EnumsAndParameters.*;
import JSON.Exceptions.*;
import JSON.FeedbackANDStatusANDOrders.CommandFeedback;
import JSON.FeedbackANDStatusANDOrders.StatusAnswer;
import JSON.Parsing.JSONInput;
import JSON.Parsing.JSONOutput;
import JSON.Parsing.JSONParser;
import JSON.FeedbackANDStatusANDOrders.YoghurtOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class JSONParserTest {

    JSONOutput.JSONOutputBuilder builder;
    JSONParser parser;

    //the two real paths later used
    String path1 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineCommands.json";
    String path2 = "03.factorymanagement/JSON/CommandsANDStatusrequests/configAllMachineStatusRequests.json";

    @Before
    public void setup() throws IOException {
        builder = new JSONOutput.JSONOutputBuilder(path1, path2);
        parser = new JSONParser();
    }

    @Test
    public void testGenerateCommandGoodCase(){
        String parsed;
        List<Parameter> params = new LinkedList<>();
        PositionParameterThreeD p1 = new PositionParameterThreeD(PositionMeaning.START, 1, 2, 4);
        PositionParameterThreeD p2 = new PositionParameterThreeD(PositionMeaning.END, 4, 2, 1);
        params.add(new Parameter(p1));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setName(CommandNames.PICK).setParams(params).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        assertTrue(true); //no Exception thrown
        assertTrue(parsed.contains("\"topicName\" : \"Gripper2Sorting\","));
        //TODO maybe add assertions (---from the following print it is generally visually clear, that the function works)
        System.out.println(parsed);

        String parsed2;
        List<Parameter> params2 = new LinkedList<>();
        params2.add(new Parameter(Colour.RED));
        try {
            parsed2 = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.SORTING).setTopicName("1.4-Sort").setName(CommandNames.EJECT).setParams(params2).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        assertTrue(true); //no Exception thrown
        assertTrue(parsed2.contains("\"topicName\" : \"1.4-Sort\","));
        //TODO maybe add assertions (---from the following print it is generally visually clear, that the function works)
        System.out.println(parsed2);

        String parsed3;
        List<Parameter> params3 = new LinkedList<>();
        params3.add(new Parameter(new NumberNatural(2)));
        try {
            parsed3 = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.INDEXEDLINE).setTopicName("1.7-Indexed").setName(CommandNames.STIRR).setParams(params3).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        assertTrue(true); //no Exception thrown
        assertTrue(parsed3.contains("\"topicName\" : \"1.7-Indexed\","));
        //TODO maybe add assertions (---from the following print it is generally visually clear, that the function works)
        System.out.println(parsed3);

        String parsed4;
        List<Parameter> params4 = new LinkedList<>();
        params4.add(new Parameter(new NumberNatural(2)));
        try {
            parsed4 = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.MULTIPROCESSING).setTopicName("2.6-Freeze").setName(CommandNames.FREEZE).setParams(params4).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        assertTrue(true); //no Exception thrown
        assertTrue(parsed4.contains("\"topicName\" : \"2.6-Freeze\","));
        //TODO maybe add assertions (---from the following print it is generally visually clear, that the function works)
        System.out.println(parsed4);
    }

    @Test
    public void testGenerateRequestGoodCase(){
        String parsed;
        List<RequestedParameter> requestedParameters = new LinkedList<>();
        requestedParameters.add(RequestedParameter.REFERENCESWITCHROTATE);
        requestedParameters.add(RequestedParameter.ROTATESTEP);
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.STATUSREQUEST).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setRequest(requestedParameters).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        assertTrue(true);
        //TODO maybe add assertions (---from the following print it is generally visually clear, that the function works)
        System.out.println(parsed);

        requestedParameters.add(RequestedParameter.ALL);
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.STATUSREQUEST).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setRequest(requestedParameters).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        assertTrue(true);
        //TODO maybe add assertions (---from the following print it is generally visually clear, that the function works)
        System.out.println(parsed);

    }

    @Test
    public void testGenerateCommandsForAllMachines(){
        String parsed;
        List<Parameter> params = new LinkedList<>();
        PositionParameterThreeD p1 = new PositionParameterThreeD(PositionMeaning.START, 1, 2, 4);
        PositionParameterThreeD p2 = new PositionParameterThreeD(PositionMeaning.END, 4, 2, 1);
        params.add(new Parameter(p1));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setName(CommandNames.PICK).setParams(params).build());
            System.out.println(parsed);
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        params.clear();
        params.add(new Parameter(BoxNumber.BOX1));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.WAREHOUSE).setTopicName("Warehouse5Sorting").setName(CommandNames.STORE).setParams(params).build());
            System.out.println(parsed);
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        params.clear();
        params.add(new Parameter(new NumberNatural(3)));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.INDEXEDLINE).setTopicName("Indexedline5Sorting").setName(CommandNames.STIRR).setParams(params).build());
            System.out.println(parsed);
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        params.clear();
        params.add(new Parameter(new NumberNatural(3)));
        params.add(new Parameter(Direction.BACKWARD));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.CONVEYOR).setTopicName("Conveyor5Sorting").setName(CommandNames.GOTOCONFIG).setParams(params).build());
            System.out.println(parsed);
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        params.clear();
        params.add(new Parameter(Direction.BACKWARD));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.CONVEYOR).setTopicName("Conveyor5Sorting").setName(CommandNames.MOVELB).setParams(params).build());
            System.out.println(parsed);
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        params.clear();
        params.add(new Parameter(new TurtlebotPosition(PositionMeaning.START, IOStationNumber.IOSTATION2)));
        params.add(new Parameter(new TurtlebotPosition(PositionMeaning.END, IOStationNumber.IOSTATION3)));
        try {
            parsed = parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.TURTLEBOT).setTopicName("Turtlebot1").setName(CommandNames.MOVE).setParams(params).build());
            System.out.println(parsed);
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGenerateCommandAndRequestBadCase() {
        Exception exception;
        String parsed;
        List<Parameter> params = new LinkedList<>();
        PositionParameterThreeD p1 = new PositionParameterThreeD(PositionMeaning.START, 1, 2, 4);
        PositionParameterThreeD p2 = new PositionParameterThreeD(PositionMeaning.END, 4, 2, 1);
        params.add(new Parameter(p1));
        params.add(new Parameter(p2));
        List<RequestedParameter> rqs = new LinkedList<>();
        //COMMANDS
        //no type set
        exception = assertThrows(MissingBuildParameterException.class,
                () -> parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setTopicName("Gripper2Sorting").setName(CommandNames.PICK).setParams(params).build()));
        //illegal command set
        exception = assertThrows(CommandNotSupportedOnThisMachineException.class,
                () -> parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setName(CommandNames.STORE).setParams(params).build()));
        //wrong number of parameters
        exception = assertThrows(WrongNumberOfParametersException.class,
                () -> parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setName(CommandNames.PICK).setParams(params).build()));

        params.remove(1);
        try {
            parser.parse(builder.setJsonType(JSONOutputType.COMMAND).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setName(CommandNames.PICK).setParams(params).build());
        } catch (JsonProcessingException | JsonApiExcpetion e) {
            throw new RuntimeException(e);
        }
        //REQUESTS
        //empty list of requests
        exception = assertThrows(MissingBuildParameterException.class,
                () -> parser.parse(builder.setJsonType(JSONOutputType.STATUSREQUEST).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setRequest(rqs).build()));
        //unknown request within the list
        rqs.add(RequestedParameter.MOTORCONVEYORBELTFORWARD);
        exception = assertThrows(RequestNotKnownOnThisMachineException.class,
                () -> parser.parse(builder.setJsonType(JSONOutputType.STATUSREQUEST).setType(CommandType.GRIPPER).setTopicName("Gripper2Sorting").setRequest(rqs).build()));
        //missing type
        exception = assertThrows(MissingBuildParameterException.class,
                () -> parser.parse(builder.setJsonType(JSONOutputType.STATUSREQUEST).setTopicName("Gripper2Sorting").setRequest(rqs).build()));
    }

    @Test
    public void testReadingGoodCase() throws JsonProcessingException, JsonApiExcpetion {
        YoghurtOrder m2 = new YoghurtOrder(JSONOutputType.ORDER, 123, Colour.RED, Flavour.STRAWBERRY, new LinkedList<>(), true, true, false);
        JSONInput i1 = new JSONInput("Order1RedStrawberryStirred", ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), m2);
        String str2 = "{\n" +
                "  \"topicName\" : \"Order1RedStrawberryStirred\",\n" +
                "  \"timestamp\" : 1679584750.742000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"ORDER\",\n" +
                "    \"orderId\" : 123,\n" +
                "    \"cup\" : \"RED\",\n" +
                "    \"yoghurtFlavour\" : \"STRAWBERRY\",\n" +
                "    \"toppings\" : [ ],\n" +
                "    \"frozen\" : true,\n" +
                "    \"stirred\" : true,\n" +
                "    \"forStoring\" : false\n" +
                "  }\n" +
                "}";
        System.out.println(str2);
        JSONInput m3 = parser.readJSONInput(str2);
        assertEquals(i1, m3); //except for timestamp
        System.out.println(m3);

        List<ParameterRequestAnswer> list = new LinkedList<>();
        list.add(new ParameterRequestAnswer(RequestedParameter.MOTORCONVEYORBELTFORWARD, true, "boolean"));
        StatusAnswer s1 = new StatusAnswer(JSONOutputType.STATUSANSWER, 2, list);
        JSONInput out = new JSONInput("GripperSorting1", ZonedDateTime.parse("2033-05-13T05:34:36.612Z[UTC]"), s1);
        String str3 = "{\n" +
                "  \"topicName\" : \"GripperSorting1\",\n" +
                "  \"timestamp\" : 1999575276.612000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"STATUSANSWER\",\n" +
                "    \"requestId\" : 2,\n" +
                "    \"answers\" : [ {\n" +
                "      \"requestedParameter\" : \"MOTORCONVEYORBELTFORWARD\",\n" +
                "      \"value\" : true,\n" +
                "      \"valueClass\" : \"boolean\"\n" +
                "    } ]\n" +
                "  }\n" +
                "}\n";
        System.out.println(str3);
        JSONInput s2 = parser.readJSONInput(str3);
        System.out.println(out);
        System.out.println(s2);
        assertEquals(out, s2); //except for timestamp

        CommandFeedback f = new CommandFeedback(JSONOutputType.FEEDBACK, 3, ExecutionStatus.INACTION, "");
        JSONInput out2 = new JSONInput("GripperSorting1", ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"), f);
        String str4 = "{\n" +
                "  \"topicName\" : \"GripperSorting1\",\n" +
                "  \"timestamp\" : 1679579703.872000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"FEEDBACK\",\n" +
                "    \"commandId\" : 3,\n" +
                "    \"status\" : \"INACTION\",\n" +
                "    \"info\" : \"\"\n" +
                "  }\n" +
                "}";
        System.out.println(str4);
        JSONInput s3 = parser.readJSONInput(str4);
        System.out.println(out2);
        System.out.println(s3);
        //assertEquals(s3.getMessage(), f);
        assertEquals(s3, out2); //except for timestamp

    }

    @Test
    public void testReadingBadCase() throws JsonProcessingException, JsonApiExcpetion {
        Exception exception;
        //missing key
        String str2 = "{\n" +
                "  \"topicName\" : \"Order1RedStrawberryStirred\",\n" +
                "  \"timestamp\" : 1679584750.742000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"ORDER\",\n" +
                "    \"yoghurtFlavour\" : \"STRAWBERRY\",\n" +
                "    \"toppings\" : [ ],\n" +
                "    \"frozen\" : true,\n" +
                "    \"stirred\" : true,\n" +
                "    \"forStoring\" : false\n" +
                "  }\n" +
                "}";
        exception = assertThrows(JsonProcessingException.class,
                () -> parser.readJSONInput(str2));
        System.out.print(exception);


        //additional key
        String str3 = "{\n" +
                "  \"topicName\" : \"GripperSorting1\",\n" +
                "  \"timestamp\" : 1999575276.612000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"STATUSANSWER\",\n" +
                "    \"requestId\" : 2,\n" +
                "    \"frozen\" : true,\n" +
                "    \"answers\" : [ {\n" +
                "      \"requestedParameter\" : \"MOTORCONVEYORBELTFORWARD\",\n" +
                "      \"value\" : true,\n" +
                "      \"valueClass\" : \"boolean\"\n" +
                "    } ]\n" +
                "  }\n" +
                "}\n";
        exception = assertThrows(JsonProcessingException.class,
                () -> parser.readJSONInput(str3));
        System.out.print(exception);


        //wrong enum
        String str4 = "{\n" +
                "  \"topicName\" : \"GripperSorting1\",\n" +
                "  \"timestamp\" : 1679579703.872000000,\n" +
                "  \"message\" : {\n" +
                "    \"jsonType\" : \"FEEDBACK\",\n" +
                "    \"commandId\" : 3,\n" +
                "    \"status\" : \"INACCCTION\",\n" +
                "    \"info\" : \"\"\n" +
                "  }\n" +
                "}";
        exception = assertThrows(JsonProcessingException.class,
                () -> parser.readJSONInput(str4));
        System.out.print(exception);

    }

}
