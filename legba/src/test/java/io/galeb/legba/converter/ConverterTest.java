package io.galeb.legba.converter;

import io.galeb.legba.conversors.Converter;
import io.galeb.legba.conversors.ConverterBuilder;
import io.galeb.legba.conversors.ConverterV1;
import io.galeb.legba.conversors.ConverterV2;
import org.junit.Assert;
import org.junit.Test;

public class ConverterTest {

    @Test
    public void shouldReturnConverterV1() throws ConverterBuilder.ConverterNotFoundException {
        Converter converter = ConverterBuilder.getConversor(ConverterV1.API_VERSION);
        Assert.assertTrue(converter instanceof ConverterV1);
    }

    @Test
    public void shouldReturnConverterV2() throws ConverterBuilder.ConverterNotFoundException {
        Converter converter = ConverterBuilder.getConversor(ConverterV2.API_VERSION);
        Assert.assertTrue(converter instanceof ConverterV2);
    }

    @Test
    public void shouldReturnConverterV1WhenApiVersionNotPassing() throws ConverterBuilder.ConverterNotFoundException {
        Converter converter = ConverterBuilder.getConversor(null);
        Assert.assertTrue(converter instanceof ConverterV1);
    }

    @Test(expected = ConverterBuilder.ConverterNotFoundException.class)
    public void shouldThrowExceptionWhenApiVersionUnknow() throws ConverterBuilder.ConverterNotFoundException {
        Converter converter = ConverterBuilder.getConversor("XXX");
    }

}
