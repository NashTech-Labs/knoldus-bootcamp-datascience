import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class LoadDataServletRun {

    public static void main(String[] args) {
        try {
            String inputFile=args[0];
            String format=args[1];
            System.out.println("inputFile= " + inputFile);
            System.out.println("format= " + format);
            final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(inputFile);
            System.out.println("Got input");
            URL url = new URL("http://localhost:8080/web.rya/loadrdf" +
                    "?format=" + format + "");
            URLConnection urlConnection = url.openConnection();
            System.out.println("After urlConnection");
            urlConnection.setRequestProperty("Content-Type", "text/plain");
            System.out.println("After setRequestProperty");
            urlConnection.setDoOutput(true);
            System.out.println("setDoOutput");

            final OutputStream os = urlConnection.getOutputStream();

            int read;
            while((read = resourceAsStream.read()) >= 0) {
                os.write(read);
            }
            resourceAsStream.close();
            os.flush();
            System.out.println("After os.flush");
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            rd.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
