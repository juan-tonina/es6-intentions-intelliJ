import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;

public class ConvertToArrow extends AnAction {
    public ConvertToArrow() {
        // Set the menu item name.
        super("Convert to arrow function");
    }

    public void actionPerformed(AnActionEvent event) {

        Project project = event.getData(PlatformDataKeys.PROJECT);
        Caret caret = event.getData(PlatformDataKeys.CARET);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        Document document = editor.getDocument();


        String selectedText = null;
        if (caret != null) {
            selectedText = caret.getSelectedText();
        }

        if (selectedText != null && selectedText.length() > 11) {
            if (selectedText.matches("\\s*function\\s*\\([\\w,\\s]*\\)\\s*\\{(.*\n?)*\\}\\s*")) {
                boolean isCodeBlock = new CodeBlockCheck(selectedText).invoke();
                if (isCodeBlock) {
                    int functionLength = 8; // Yeah...
                    int selectionStart = caret.getSelectionStart();
                    int functionIndex = selectedText.indexOf("function");
                    String params = selectedText.substring(functionIndex + functionLength, selectedText.indexOf(')') + 1);
                    int paramsIndex = editor.getDocument().getText().indexOf(params, selectionStart);
                    Runnable runnable = () -> document.replaceString(selectionStart, paramsIndex + params.length(), (params.indexOf('(') != -1 ? params.trim() + " =>" : "(" + params.trim() + ") =>"));
                    WriteCommandAction.runWriteCommandAction(project, runnable);

                    // Show balloon message
                    StatusBar statusBar = WindowManager.getInstance()
                            .getStatusBar(PlatformDataKeys.PROJECT.getData(event.getDataContext()));
                    JBPopupFactory.getInstance().createHtmlTextBalloonBuilder("Converted to arrow function! Whooosh! Magic!", null, JBColor.CYAN, null)
                            .setFadeoutTime(5500)
                            .createBalloon()
                            .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                                    Balloon.Position.atRight);
                }

            }
        } else {
            Messages.showMessageDialog(project, "My super algorithm does not recognize this as a function",
                    "Nope", Messages.getErrorIcon());
        }

    }

    /**
     * This is mostly duplicated, but let's pretend it is not
     * @param e some action event from which we get the project, data context and file language
     */
    public void update(AnActionEvent e) {
        super.update(e);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (file != null) {
            boolean visible = project != null && ((LanguageFileType) file.getFileType()).getLanguage().getID().equals("JavaScript");
            // Visibility
            e.getPresentation().setVisible(visible);
            // Enable or disable
            e.getPresentation().setEnabled(visible);
        } else {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
        }

    }
}
