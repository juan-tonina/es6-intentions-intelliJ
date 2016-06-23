import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;

/**
 * Created by Juan on 22/06/2016
 */
class ConvertToTemplateString extends AnAction {

    ConvertToTemplateString() {
        super("Convert to template string");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Caret caret = event.getData(PlatformDataKeys.CARET);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        Document document = null;
        if (editor != null) {
            document = editor.getDocument();
        }
        PsiFile psiFile = null;
        if (project != null && document != null) {
            psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        }

        PsiElement psiElement = null;
        if (psiFile != null && caret != null) {
            psiElement = psiFile.findElementAt(caret.getOffset());
        }

        // Let's find the first binary expression
        while (psiElement != null && !(psiElement.getNode() != null &&
                psiElement.getNode().getElementType().toString().equals("JS:BINARY_EXPRESSION"))) {
            psiElement = psiElement.getParent();
        }

        // Let's find the entire expression
        while (psiElement != null && !(psiElement.getNode() != null &&
                !psiElement.getParent().getNode().getElementType().toString().equals("JS:BINARY_EXPRESSION"))) {
            psiElement = psiElement.getParent();
        }

        if (psiElement != null) {


            String text = "a = `" + transformToTemplateString(psiElement, "") + "`";

            PsiFile fileFromText = PsiFileFactory.getInstance(project).createFileFromText(text, psiFile);

            Runnable runnable = null;
            if (fileFromText != null) {
                final PsiElement finalPsiElement = psiElement;
                runnable = () -> finalPsiElement.replace(fileFromText.getFirstChild().getFirstChild().getLastChild());
            }
            if (runnable != null) {
                WriteCommandAction.runWriteCommandAction(project, runnable);
            }
        }


    }


    /**
     * This should, I think, create the template string... it is recursive.
     * It should be easy.... r-right?
     *
     * @param psiElement the binary expression (or parenthesised expression)
     * @param s          empty string
     * @return the actual string
     */
    private String transformToTemplateString(PsiElement psiElement, String s) {

        for (PsiElement elem : psiElement.getChildren()) {
            if (elem != null) {
                String s1 = elem.getNode().getElementType().toString();
                switch (s1) {
                    // (1 + a()) is scaped as ${1 + a}
                    case "JS:PARENTHESIZED_EXPRESSION":
                        s += "${" + elem.getText().substring(1, elem.getTextLength() - 1) + "}";
                        break;
                    // "" + 1 is sent again recursively
                    case "JS:BINARY_EXPRESSION":
                        s = transformToTemplateString(elem, s);
                        break;
                    // someVar is scaped as ${someVar}
                    case "JS:REFERENCE_EXPRESSION":
                        s += "${" + elem.getText() + "}";
                        break;
                    // Strings and numbers are concatenated
                    case "JS:LITERAL_EXPRESSION":
                        if (elem.getText().startsWith("\'") || elem.getText().startsWith("\"")) {
                            s += elem.getText().substring(1, elem.getTextLength() - 1);
                        } else {
                            s += elem.getText();
                        }
                        break;
                }
            }
        }


        return s;
    }

    /**
     * This is mostly duplicated, but let's pretend it is not
     *
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
