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

import java.util.Objects;

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
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

        PsiElement psiElement = psiFile.findElementAt(caret.getOffset());


        while (psiElement != null && (!psiElement.getText().startsWith("function") || psiElement.getText().equals("function"))) {
            psiElement = psiElement.getParent();
        }

        String text = psiElement.getText();
        text = text.replaceFirst("function", "");

        if (text.trim().startsWith("(")) {
            text = text.replaceFirst("\\)", ") =>");

            PsiFile fileFromText = PsiFileFactory.getInstance(project).createFileFromText(text, psiFile);

            PsiElement finalPsiElement = psiElement;
            Runnable runnable = () -> finalPsiElement.replace(fileFromText.getLastChild());
            WriteCommandAction.runWriteCommandAction(project, runnable);
        }

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
