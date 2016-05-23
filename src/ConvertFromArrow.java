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

public class ConvertFromArrow extends AnAction {
    public ConvertFromArrow() {
        // Set the menu item name.
        super("Convert from arrow function");
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

        if (selectedText != null && selectedText.length() > 3) {
            if (selectedText.matches("((\\s*\\([\\w,\\s]*\\)\\s*=>)|([\\w\\s]*\\s*=>))\\s*\\{(.*\n?)*\\}\\s*")) {
                boolean isCodeBlock = new CodeBlockCheck(selectedText).invoke();
                if (isCodeBlock) {
                    replaceOnMethod(event, project, caret, editor, document, selectedText, false);
                }

            } else if (selectedText.matches("((\\s*\\([\\w,\\s]*\\)\\s*=>)|([\\w\\s]*\\s*=>))(.|\\s)*")) {
                char[] chars = selectedText.toCharArray();


                boolean doubleQuotes = false;
                boolean singleQuotes = false;
                int curlyBrackets = 0;
                int squareBrackets = 0;
                int brackets = 0;

                int i;
                // I'm so sorry for the following label
                loop:
                for (i = 0; i < chars.length; i++) {

                    switch (chars[i]) {
                        case '"':
                            if (!singleQuotes) {
                                doubleQuotes = !doubleQuotes;
                            }
                            break;
                        case '\'':
                            if (!doubleQuotes) {
                                singleQuotes = !singleQuotes;
                            }
                            break;
                        case '{':
                            if (!doubleQuotes && !singleQuotes) {
                                ++curlyBrackets;
                            }
                            break;
                        case '}':
                            if (!doubleQuotes && !singleQuotes) {
                                --curlyBrackets;
                            }
                            break;
                        case '[':
                            if (!doubleQuotes && !singleQuotes) {
                                ++squareBrackets;
                            }
                            break;
                        case ']':
                            if (!doubleQuotes && !singleQuotes) {
                                --squareBrackets;
                            }
                            break;
                        case '(':
                            if (!doubleQuotes && !singleQuotes) {
                                ++brackets;
                            }
                            break;
                        case ')':
                            if (!doubleQuotes && !singleQuotes) {
                                --brackets;
                            }
                            break;
                        case '\n':
                            if (!doubleQuotes && !singleQuotes && brackets == 0 && squareBrackets == 0 && curlyBrackets == 0) {
                                // You know... in case someone is splitting a string with a plus sign in the middle of an
                                //  arrow function without curly brackets... The little specific f*cker
                                if (i > 0 && chars[i - 1] != '+') {

                                    break loop; // HAHA! No one expects a "break something"
                                }
                            }
                            break;
                        case ';':
                            if (!doubleQuotes && !singleQuotes && brackets == 0 && squareBrackets == 0 && curlyBrackets == 0) {
                                break loop; // HAHA! No one expects a (two) "break something"(s)
                            }
                            break;

                    }

                }
                if (selectedText.substring(i).matches("\\s*;?")) {
                    replaceOnMethod(event, project, caret, editor, document, selectedText, true);
                }


            }
        } else {
            Messages.showMessageDialog(project, "My super algorithm does not recognize this as an arrow function",
                    "Nope", Messages.getErrorIcon());
        }

    }

    private void replaceOnMethod(AnActionEvent event, Project project, Caret caret, Editor editor, Document document, String selectedText, boolean addReturnAndBrackets) {
        int selectionStart = caret.getSelectionStart();
        int arrowIndex = selectedText.indexOf("=>");
        int arrowGlobalIndex = editor.getDocument().getText().indexOf("=>", selectionStart);
        String params = selectedText.substring(0, arrowIndex);
        Runnable runnable = () -> {
            int arrowLength = 2; // Yeah...
            String charSequence;
            if (addReturnAndBrackets) {
                charSequence = "function" + (params.indexOf('(') != -1 ? params.trim() + "{ return " : "(" + params.trim() + ") { return ");
                document.insertString(caret.getSelectionEnd(), "}");
            } else {
                charSequence = "function" + (params.indexOf('(') != -1 ? params.trim() : "(" + params.trim() + ")");
            }
            document.replaceString(selectionStart, arrowGlobalIndex + arrowLength, charSequence);
        };
        WriteCommandAction.runWriteCommandAction(project, runnable);

        // Show balloon message
        StatusBar statusBar = WindowManager.getInstance()
                .getStatusBar(PlatformDataKeys.PROJECT.getData(event.getDataContext()));
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder("Converted arrow function to... non-arrow function?", null, JBColor.CYAN, null)
                .setFadeoutTime(5500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                        Balloon.Position.atRight);
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
