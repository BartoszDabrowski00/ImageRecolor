import services.RecolorTool;

public class PT6 {
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        String source = currentDir + "\\pic_source\\";
        String destination = currentDir + "\\pic_dst\\";
        new RecolorTool(source, destination).run();
    }
}
