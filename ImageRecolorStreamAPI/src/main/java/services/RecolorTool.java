package services;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class RecolorTool {
    private final String source;
    private final String destination;

    public void run(){
        ForkJoinPool pool = ForkJoinPool.commonPool();
        double time = System.currentTimeMillis();
        List<Path> files;
        try(Stream<Path> stream = Files.list(Paths.get(source))){
            files = stream.collect(Collectors.toList());
            List<Pair<String, BufferedImage>> originalFiles = new ArrayList<>();
            files.forEach(x->{
                try {
                    BufferedImage image = ImageIO.read(x.toFile());
                    String name = x.getFileName().toString();
                    originalFiles.add(Pair.of(name, image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            pool.submit(()-> originalFiles.stream().parallel().forEach(x->{
                BufferedImage originalImg = x.getRight();
                BufferedImage modifiedImg = modifyImage(originalImg);
                String newName = destination + x.getLeft();
                saveFile(newName, modifiedImg);
            })).get();
        }
        catch(IOException | InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis()-time)/1000);
    }

    private Color modifyColor(int rgb){
        Color prevColor = new Color(rgb);
        int red = prevColor.getRed();
        int green = prevColor.getGreen();
        int blue = prevColor.getBlue();
        return new Color(green, red, blue);
    }

    private BufferedImage modifyImage(BufferedImage originalImg){
        int width = originalImg.getWidth();
        int height = originalImg.getHeight();
        BufferedImage modifiedImg = new BufferedImage(width, height, originalImg.getType());
        for(int i=0; i<width;i++){
            for(int j=0;j<height;j++){
                Color modifiedColor = modifyColor(originalImg.getRGB(i, j));
                modifiedImg.setRGB(i, j, modifiedColor.getRGB());
            }
        }
        return modifiedImg;
    }

    private void saveFile(String newName, BufferedImage modifiedImg){
        File output = new File(newName);
        try {
            ImageIO.write(modifiedImg, "jpg", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
