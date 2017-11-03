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

/**
 * Created by Juan on 22/06/2016
 */
internal class ConvertToTemplateString : AnAction("Convert to template string") {

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

        // Let's find the first binary expression
        while (psiElement != null && !(psiElement.node != null && psiElement.node.elementType.toString() == "JS:BINARY_EXPRESSION")) {
            psiElement = psiElement.parent
        }

        // Let's find the entire expression
        while (psiElement != null && !(psiElement.node != null && psiElement.parent.node.elementType.toString() != "JS:BINARY_EXPRESSION")) {
            psiElement = psiElement.parent
        }

        if (psiElement != null) {


            val text = "a = `" + transformToTemplateString(psiElement, "") + "`"

            val fileFromText = PsiFileFactory.getInstance(project!!).createFileFromText(text, psiFile!!)

            var runnable: Runnable? = null
            if (fileFromText != null) {
                val finalPsiElement = psiElement
                runnable = Runnable { finalPsiElement.replace(fileFromText.firstChild.firstChild.lastChild) }
            }
            if (runnable != null) {
                WriteCommandAction.runWriteCommandAction(project, runnable)
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
    private fun transformToTemplateString(psiElement: PsiElement?, s: String): String {
        var str = s

        for (elem in psiElement!!.children) {
            if (elem != null) {
                val s1 = elem.node.elementType.toString()
                when (s1) {
                // (1 + a()) is scaped as ${1 + a}
                    "JS:PARENTHESIZED_EXPRESSION" -> str += "\${" + elem.text.substring(1, elem.textLength - 1) + "}"
                // "" + 1 is sent again recursively
                    "JS:BINARY_EXPRESSION" -> str = transformToTemplateString(elem, str)
                // someVar is scaped as ${someVar}
                    "JS:REFERENCE_EXPRESSION" -> str += "\${" + elem.text + "}"
                // Strings and numbers are concatenated
                    "JS:LITERAL_EXPRESSION" -> if (elem.text.startsWith("\'") || elem.text.startsWith("\"")) {
                        str += elem.text.substring(1, elem.textLength - 1)
                    } else {
                        str += elem.text
                    }
                }
            }
        }


        return str
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
}
