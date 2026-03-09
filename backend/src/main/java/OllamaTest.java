import java.io.*;

public class OllamaTest {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ollama", "run", "gemma3:1b"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Send prompt to Ollama
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write("List 5 ingredients for spaghetti\n");
            writer.flush();
            writer.close();

            // Read response from Ollama
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
