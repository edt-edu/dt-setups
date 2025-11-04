import java.io.*;

public class PythonRunner { 
    public static void main(String[] args) {
        try {
            // Adjust the path to the Python script if necessary
            ProcessBuilder pb = new ProcessBuilder("python3", "Demonstrator Script and Model\\SysOnScript.py");
            Process process = pb.start();

            // Send data to stdin
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
            // Adjust the string here. It is important that the SysMLv2 model is passed as a text block,
            // otherwise there will be problems and the model will not be read completely.
            String sysMLv2Model = """
            part def test{}
            part def test2{
            part test3{}}
            part def test4{}
            """;
            writer.write(sysMLv2Model);
            writer.flush();
            writer.close();

            // Read output from Python, if desired. Otherwise, this part can be omitted.
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Python Output: " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
