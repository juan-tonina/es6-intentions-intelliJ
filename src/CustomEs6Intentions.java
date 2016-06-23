import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class CustomEs6Intentions implements ApplicationComponent {
    // Returns the component name (any unique string value).
    @NotNull
    public String getComponentName() {
        return "CustomEs6Intentions";
    }

    public void initComponent() {
        ActionManager am = ActionManager.getInstance();
        ConvertFromArrow action = new ConvertFromArrow();
        ConvertToArrow action2 = new ConvertToArrow();
        ConvertToTemplateString action3 = new ConvertToTemplateString();

        // Passes an instance of your custom ConvertFromArrow class to the registerAction method of the ActionManager class.
        am.registerAction("Convert from arrow function", action);
        am.registerAction("Convert to arrow function", action2);
        am.registerAction("Convert to template string", action3);

        // Gets an instance of the WindowMenu action group.
        DefaultActionGroup jsHierarchyPopUpMenu = (DefaultActionGroup) am.getAction("RefactoringMenu");

        // Adds a separator and a new menu command to the WindowMenu group on the main menu.
        jsHierarchyPopUpMenu.addSeparator();
        jsHierarchyPopUpMenu.add(action);
        jsHierarchyPopUpMenu.add(action2);
        jsHierarchyPopUpMenu.addSeparator();
        jsHierarchyPopUpMenu.add(action3);
    }

    // Disposes system resources.
    public void disposeComponent() {
    }

}
