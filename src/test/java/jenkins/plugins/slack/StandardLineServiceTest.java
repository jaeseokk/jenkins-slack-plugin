package jenkins.plugins.slack;

import org.junit.Test;

import jenkins.plugins.line.StandardLineService;

public class StandardLineServiceTest {
    @Test
    public void sendMessageShouldSuccess() {
        StandardLineService service = new StandardLineService("Q8YrYDWkIwi4ScyZv4JHTaZBOEZSuahN7olfEcgB8AW");
        service.send("What a awesome line message!!!");
    }
}
