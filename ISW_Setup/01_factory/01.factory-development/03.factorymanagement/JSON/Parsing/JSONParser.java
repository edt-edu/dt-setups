package JSON.Parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * Performs the actual parsing/deserializing process
 */
public class JSONParser {

    private final ObjectMapper mapper;


    /**
     * Creates a Parser object, that is able to take input or create output, depending on the functions that are called.
     */
    public JSONParser() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        //To avoid the first parsing process at runtime to take much longer than expected,
        //maybe make mapper read example Json-Strings for each class
        try{
            //for example like this
            //mapper.readValue("{\"yoghurtId\":\"hallo\",\"orderId\":1}", YoghurtOrder.class); (not the current version of this string)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The function parse the given JSONOutput object into a JSON String representation
     *
     * @param jsonOutput the object that is being serialized
     * @return the JSON String representation of the above object
     * @throws JsonProcessingException (should never be thrown as JSONOutput is always conform as its build with OutputBuilder)
     */
    public String parse(JSONOutput jsonOutput) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonOutput);
    }

    /**
     * The function deserializes the given JSONInput String representation into a JSONInput object
     *
     * @param json the string that is being deserialized
     * @return the JSONInput object of the above string
     * @throws JsonProcessingException (when thrown likely an error in the jsonString creation in python)
     */
    public JSONInput readJSONInput(String json) throws JsonProcessingException {
        return mapper.readerFor(JSONInput.class).readValue(json);
    }

}
