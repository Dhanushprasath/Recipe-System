package com.recipe.backend.service;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SpellCheckService {

    // SINGLE shared instance (very important)
    private static JLanguageTool langTool;

    public SpellCheckService() {
        if (langTool == null) {
            langTool = new JLanguageTool(
                    Languages.getLanguageForShortCode("en-US")
            );
        }
    }

    // Auto-correct text
    public String autoCorrect(String text) {
        try {
            List<RuleMatch> matches = langTool.check(text);

            StringBuilder correctedText = new StringBuilder(text);
            int offset = 0;

            for (RuleMatch match : matches) {
                List<String> suggestions = match.getSuggestedReplacements();
                if (!suggestions.isEmpty()) {
                    String replacement = suggestions.get(0);

                    correctedText.replace(
                            match.getFromPos() + offset,
                            match.getToPos() + offset,
                            replacement
                    );

                    offset += replacement.length()
                            - (match.getToPos() - match.getFromPos());
                }
            }

            return correctedText.toString();

        } catch (IOException e) {
            return text; // safe fallback
        }
    }
}
