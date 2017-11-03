import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class Es6TemplateProvider : DefaultLiveTemplatesProvider {
    override fun getDefaultLiveTemplateFiles(): Array<String> {
        return arrayOf("liveTemplates/es6Intentions")
    }

    override fun getHiddenLiveTemplateFiles(): Array<String?> {
        return arrayOfNulls(0)
    }
}