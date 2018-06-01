package io.github.guilhermedelemos;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Log {

    private String arquivo;

    public Log() {
        super();
        this.arquivo = "./log.txt";
    }

    public Log(String nomeArquivo) {
        this.arquivo = "./" + nomeArquivo;
    }

    public void add(String s) {
        s = s + System.getProperty("line.separator");
        byte data[] = s.getBytes();
        Path p = Paths.get(this.arquivo);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, APPEND))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }
    }

    public void add(String s, boolean date) {
        if (date) {
            this.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()) + ": " + s);
        } else {
            this.add(s);
        }
    }

}
