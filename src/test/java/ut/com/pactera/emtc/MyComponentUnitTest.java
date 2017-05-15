package ut.com.pactera.emtc;

import org.junit.Test;
import com.pactera.emtc.api.MyPluginComponent;
import com.pactera.emtc.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}