package org.mellowd.jupyter;

import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.Header;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.mellowd.compiler.MellowD;
import org.mellowd.compiler.MellowDCompiler;
import org.mellowd.compiler.MellowDLexer;
import org.mellowd.compiler.MellowDParser;
import org.mellowd.compiler.ParseErrorListener;
import org.mellowd.compiler.ParseException;
import org.mellowd.io.Compiler;
import org.mellowd.io.ResourceSourceFinder;
import org.mellowd.io.SourceFinder;
import org.mellowd.io.WavIODelegate;
import org.mellowd.midi.TimingEnvironment;
import org.mellowd.plugin.PluginLoadException;
import org.mellowd.plugin.PluginManager;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class IMellowDKernel extends BaseKernel {
    private static final String WAVE_MIME_TYPE = "audio/wav";

    private static final byte[] AUDIO_ELEMENT_PRE_DATA = (
            "<audio controls=\"controls\" autoplay=\"autoplay\">\n" +
            "<source src=\"data:" + WAVE_MIME_TYPE + ";base64,"
    ).getBytes(StandardCharsets.UTF_8);
    private static final byte[] AUDIO_ELEMENT_POST_DATA = (
            "\" type=\"" + WAVE_MIME_TYPE + "\" />\n" +
            "Your browser does not support the audio element.\n" +
            "</audio>"
    ).getBytes(StandardCharsets.UTF_8);

    private final LanguageInfo languageInfo;
    private final String banner;
    private final List<LanguageInfo.Help> helpLinks;

    private final MellowD mellowD;
    private final MellowDCompiler compiler;
    private final PluginManager plugins;

    private final WavIODelegate wavWriter;

    public IMellowDKernel() {
        this.languageInfo = new LanguageInfo.Builder("MellowD")
                .mimetype("text/x-java-source")
                .fileExtension(".mlod")
                .pygments("groovy")
                .codemirror("mellowd")
                .build();
        this.banner = String.format("MellowD %s :: IMellowD kernel %s \nProtocol v%s implementation by %s %s",
                Compiler.VERSION,
                IMellowD.VERSION,
                Header.PROTOCOL_VERISON,
                KERNEL_META.getOrDefault("project", "UNKNOWN"),
                KERNEL_META.getOrDefault("version", "UNKNOWN")
        );
        this.helpLinks = new ArrayList<>(1);
        this.helpLinks.add(new LanguageInfo.Help("MellowD", "https://github.com/SpencerPark/MellowD"));

        SourceFinder srcFinder = new ResourceSourceFinder(Compiler.FILE_EXTENSION);
        TimingEnvironment timingEnv = new TimingEnvironment(4, 4, 120);

        this.mellowD = new MellowD(srcFinder, timingEnv);
        this.compiler = new MellowDCompiler(this.mellowD);
        this.plugins = new PluginManager();
        try {
            this.plugins.applySome(this.mellowD, Collections.singletonList("bjorklund"));
        } catch (PluginLoadException e) {
            System.err.printf("WARNING: Could not load plugin. %s", e.getLocalizedMessage());
        }

        this.wavWriter = new WavIODelegate(null);
        this.wavWriter.setSampleSize(16);
    }

    @Override
    public String getBanner() {
        return this.banner;
    }

    @Override
    public List<LanguageInfo.Help> getHelpLinks() {
        return this.helpLinks;
    }

    @Override
    public LanguageInfo getLanguageInfo() {
        return this.languageInfo;
    }

    @Override
    public DisplayData eval(String code) throws Exception {
        CharStream input = CharStreams.fromString(code);
        MellowDLexer lexer = new MellowDLexer(input);

        TokenStream tokens = new CommonTokenStream(lexer);
        MellowDParser parser = new MellowDParser(tokens);

        ParseErrorListener errorListener = new ParseErrorListener();
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        MellowDParser.SongContext parseTree = parser.song();
        if (errorListener.encounteredError())
            throw new ParseException(errorListener.getErrors());

        if (!parseTree.importStmt().isEmpty()) {
            //Compile the dependencies
            parseTree.importStmt().forEach(this.compiler::visitImportStmt);
        }

        this.compiler.visitSong(parseTree);

        Sequence sequence;
        try {
            sequence = mellowD.execute().toSequence();
        } catch (Exception e) {
            throw new ExecutionException("Error executing code: " + e.getLocalizedMessage(), e);
        }

        boolean hasNoteOn = false;
        for (Track t : sequence.getTracks()) {
            for (int i = 0; i < t.size(); i++) {
                MidiEvent e = t.get(i);
                if ((e.getMessage().getStatus() & 0xF0) == ShortMessage.NOTE_ON) {
                    hasNoteOn = true;
                    break;
                }
            }
            if (hasNoteOn) break;
        }
        if (!hasNoteOn) return null;

        /*
         *  <audio controls="controls" {autoplay}>
                <source src="{src}" type="{type}" />
                Your browser does not support the audio element.
            </audio>
         */
        OutputStream data = new ByteArrayOutputStream();
        OutputStream b64 = Base64.getEncoder().wrap(data);

        data.write(AUDIO_ELEMENT_PRE_DATA);
        this.wavWriter.save(sequence, b64);
        data.write(AUDIO_ELEMENT_POST_DATA);

        data.close();

        DisplayData result = new DisplayData(sequence.toString());
        result.putHTML(data.toString());
        return result;
    }
}
