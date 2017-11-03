import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory

class ConvertFromArrow : AnAction("Convert from arrow function") {

    override fun actionPerformed(event: AnActionEvent) {

        val project = event.getData(PlatformDataKeys.PROJECT)
        val caret = event.getData(PlatformDataKeys.CARET)
        val editor = event.getData(PlatformDataKeys.EDITOR)
        var document: Document? = null
        if (editor != null) {
            document = editor.document
        }
        var psiFile: PsiFile? = null
        if (project != null && document != null) {
            psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
        }

        var psiElement: PsiElement? = null
        if (psiFile != null && caret != null) {
            psiElement = psiFile.findElementAt(caret.offset)
        }


        // I'm SURE that there is a better way of doing this... but again, I'm usually wrong
        while (psiElement != null && !(psiElement.node != null &&
                psiElement.node.elementType.toString() == "JS:FUNCTION_EXPRESSION" &&
                !psiElement.text.startsWith("function"))) {
            psiElement = psiElement.parent
        }

        if (psiElement != null) {
            var text = psiElement.text

            if (!text.endsWith("}")) {
                text = text.replaceFirst("=>".toRegex(), "{ return")
                text += ";}"
            } else {
                text = text.replaceFirst("=>".toRegex(), "")
            }
            if (text.startsWith("(")) {
                text = "function" + text
            } else {
                // This should be "functionExpression.ParamList.param.length"
                val textLength = psiElement.firstChild.firstChild.textLength
                text = "function (" + text.substring(0, textLength) + ")" + text.substring(textLength)
            }

            text = "a = " + text

            val fileFromText = PsiFileFactory.getInstance(project!!).createFileFromText(text, psiFile!!)

            val finalPsiElement = psiElement
            var runnable: Runnable? = null
            if (fileFromText != null) {
                runnable = Runnable { finalPsiElement.replace(fileFromText.lastChild.lastChild.lastChild) }
            }
            if (runnable != null) {
                WriteCommandAction.runWriteCommandAction(project, runnable)
            }
        }


    }


    /**
     * This is mostly duplicated, but let's pretend it is not
     *
     * @param e some action event from which we get the project, data context and file language
     */
    override fun update(e: AnActionEvent?) {
        super.update(e)
        val project = e!!.getData(PlatformDataKeys.PROJECT)
        val file = PlatformDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        if (file != null) {
            val visible: Boolean = try {
                project != null && (file.fileType as LanguageFileType).language.id == "JavaScript"
            } catch (e: Exception) {
                false
            }
            // Visibility
            e.presentation.isVisible = visible
            // Enable or disable
            e.presentation.isEnabled = visible
        } else {
            e.presentation.isVisible = false
            e.presentation.isEnabled = false
        }

    }

}// Set the menu item name.
