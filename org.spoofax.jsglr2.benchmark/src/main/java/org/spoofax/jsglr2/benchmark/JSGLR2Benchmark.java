package org.spoofax.jsglr2.benchmark;

import java.io.IOException;
import java.net.URISyntaxException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.JSGLR2Variants;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;
import org.spoofax.terms.ParseError;

public abstract class JSGLR2Benchmark extends BaseBenchmark {

    protected IParser<?, ?> parser;
    protected JSGLR2<?, ?, ?> jsglr2;
    
    @Param({"SymbolRule", "Hybrid"})
    public JSGLR2Variants.ParseForestRepresentation parseForestRepresentation;
    
    @Param({"true", "false"})
    public boolean elkhoundStack;
    
    @Param({"true", "false"})
    public boolean elkhoundReducing;
    
    protected abstract void prepareParseTable() throws ParseError, ParseTableReadException, IOException, InvalidParseTableException, InterruptedException, URISyntaxException;
    
    @Setup
    public void prepare() throws ParseError, ParseTableReadException, IOException, InvalidParseTableException, InterruptedException, URISyntaxException {
        prepareParseTable();
        
        IParseTable parseTable = ParseTableReader.read(parseTableTerm);

        parser = JSGLR2Variants.getParser(parseTable, parseForestRepresentation, elkhoundStack, elkhoundReducing);
        jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, parseForestRepresentation, elkhoundStack, elkhoundReducing);
        
        inputs = getInputs();
    }
    
    @Benchmark
    public void jsglr2parse(Blackhole bh) throws ParseException {
        for (Input input : inputs)
            bh.consume(parser.parseUnsafe(
                input.content,
                input.filename
            ));
    }
    
    @Benchmark
    public void jsglr2parseAndImplode(Blackhole bh) throws ParseException {
        for (Input input : inputs)
            bh.consume(jsglr2.parseUnsafe(
                input.content,
                input.filename
            ));
    }

}