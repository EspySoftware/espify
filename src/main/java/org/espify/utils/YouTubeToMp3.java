package utils;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;

import java.io.File;
import java.util.List;

public class YouTubeToMp3 {
    public static void main(String[] args) {
        String song;
        song =  downloadAudio("9W6AN_eQeZo");
    }
    public static String downloadAudio(String videoId) {
        YoutubeDownloader downloader = new YoutubeDownloader();
        File outputDir = new File("downloads");
        String exit = "";
        try {
            RequestVideoInfo request = new RequestVideoInfo(videoId);
            Response<VideoInfo> response = downloader.getVideoInfo(request);
            VideoInfo videoInfo = response.data();
            VideoDetails details = videoInfo.details();

            String sanitizedTitle = details.title().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            
            File existingMp3 = findExistingMp3(outputDir, sanitizedTitle);
            if (existingMp3 != null) {
                exit = details.title() + "|" + existingMp3.getAbsolutePath();
                return exit;
            }

            List<AudioFormat> audioFormats = videoInfo.audioFormats();
            if (audioFormats.isEmpty()) {
                System.out.println("No se encontraron formatos de audio para este video.");
            }

            // Seleccionar el mejor formato de audio (por ejemplo, MP3)
            AudioFormat bestAudioFormat = audioFormats.get(0);
            // Directorio de destino para la descarga
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }

            // Crea la solicitud de descarga
            RequestVideoFileDownload downloadRequest = new RequestVideoFileDownload(bestAudioFormat)
                .saveTo(outputDir) 
                .renameTo(sanitizedTitle) 
                .overwriteIfExists(true);   
            
            // Descarga el archivo de audio
            File downloadedFile = downloader.downloadVideoFile(downloadRequest).data();
            outputDir = convertToMp3(downloadedFile);
            exit = details.title() + "|" + outputDir.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Error al descargar el video: " + e.getMessage());
            e.printStackTrace();
        }
        return exit;
    }

    private static File convertToMp3(File inputFile) {
        File outputFile = null;
        try {
            String outputFileName = inputFile.getAbsolutePath().replace(".m4a", ".mp3");
            outputFile = new File(outputFileName);
            ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-i", inputFile.getAbsolutePath(), outputFileName);
            
            // Redirect output and error streams to avoid writing to the terminal
            builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            
            builder.start().waitFor();
        } catch (Exception e) {
            System.err.println("Error al convertir el archivo a MP3: " + e.getMessage());
        } finally {
            inputFile.delete();
        }
        return outputFile;
    }

    public static File findExistingMp3(File directory, String baseName) {
        File potentialMp3 = new File(directory, baseName + ".mp3");
        if (potentialMp3.exists() && potentialMp3.isFile()) {
            return potentialMp3;
        }
        return null;
    }
}