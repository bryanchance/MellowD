package org.mellowd.jupyter;

import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import org.mellowd.io.Compiler;
import org.mellowd.jupyter.live.IMellowDLiveKernel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class IMellowD {
    public static final String VERSION;

    static {
        Properties metadata = new Properties();
        try {
            metadata.load(Compiler.class.getResourceAsStream("/mellowd-compiler-metadata.properties"));
        } catch (IOException ignored) {
        }

        VERSION = metadata.getProperty("version", "UNKNOWN");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new IllegalArgumentException("Missing connection file argument");

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile));

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        String sfVar = System.getenv("MELLOWD_SF_PATH");
        IMellowDLiveKernel kernel = new IMellowDLiveKernel(sfVar != null ? Paths.get(sfVar) : null);
        kernel.becomeHandlerForConnection(connection);

        connection.connect();
        connection.waitUntilClose();
    }
}
