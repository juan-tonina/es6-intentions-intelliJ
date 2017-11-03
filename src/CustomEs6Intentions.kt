import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ApplicationComponent

class CustomEs6Intentions : ApplicationComponent {
    // Returns the component name (any unique string value).
    override fun getComponentName(): String {
        return "CustomEs6Intentions"
    }

    override fun initComponent() {
        val am = ActionManager.getInstance()
        val action = ConvertFromArrow()
        val action2 = ConvertToArrow()
        val action3 = ConvertToTemplateString()

        // Passes an instance of your custom ConvertFromArrow class to the registerAction method of the ActionManager class.
        am.registerAction("Convert from arrow function", action)
        am.registerAction("Convert to arrow function", action2)
        am.registerAction("Convert to template string", action3)

        // Gets an instance of the WindowMenu action group.
        val jsHierarchyPopUpMenu = am.getAction("RefactoringMenu") as DefaultActionGroup

        // Adds a separator and a new menu command to the WindowMenu group on the main menu.
        jsHierarchyPopUpMenu.addSeparator()
        jsHierarchyPopUpMenu.add(action)
        jsHierarchyPopUpMenu.add(action2)
        jsHierarchyPopUpMenu.addSeparator()
        jsHierarchyPopUpMenu.add(action3)
    }

    // Disposes system resources.
    override fun disposeComponent() {}

}
