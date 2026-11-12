package ru.givler.mbo.integration.thaumcraft.client.render;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.*;
import thaumcraft.common.lib.research.*;

/** Resolve the live TC4 registry after scripts; the note remains the authority for its puzzle. */
final class ResearchScrollContent {
    final String title, text;
    final boolean complete, runes;
    final List<Aspect> aspects;
    private ResearchScrollContent(String title, String text, boolean complete, boolean runes, List<Aspect> aspects) {
        this.title=title;this.text=text;this.complete=complete;this.runes=runes;this.aspects=Collections.unmodifiableList(aspects);
    }
    static ResearchScrollContent read(ItemStack stack, Predicate<String> unlocked) {
        ResearchNoteData note=ResearchManager.getData(stack);
        boolean unidentified=stack==null || stack.getItemDamage()==24 || stack.getItemDamage()==42
                || note==null || note.key==null || note.key.isEmpty();
        List<Aspect> aspects=new ArrayList<Aspect>();
        if(!unidentified) {
            Set<Aspect> unique=new HashSet<Aspect>();
            for(ResearchManager.HexEntry entry:note.hexEntries.values())
                if(entry!=null && entry.type==1 && entry.aspect!=null)unique.add(entry.aspect);
            aspects.addAll(unique);aspects.sort(Comparator.comparing(Aspect::getTag));
        }
        if(unidentified)return new ResearchScrollContent(tr("mbo.research_scroll.unknown"),runes(),false,true,aspects);
        ResearchItem research=ResearchCategories.getResearch(note.key);
        if(research==null)return new ResearchScrollContent(tr("mbo.research_scroll.missing"),
                note.complete?tr("mbo.research_scroll.unavailable"):runes(),note.complete,!note.complete,aspects);
        if(aspects.isEmpty() && research.tags!=null)
            for(Aspect aspect:research.tags.getAspectsSorted())if(aspect!=null)aspects.add(aspect);
        String title=research.getName();
        if(title==null || title.isEmpty() || title.equals("tc.research_name."+research.key))title=tr("mbo.research_scroll.unknown");
        if(!note.complete)return new ResearchScrollContent(title,runes(),false,true,aspects);
        String text="",override="mbo.research_scroll."+note.key+".text";
        if(StatCollector.canTranslate(override))text=plain(tr(override));
        if(text.isEmpty() && research.getPages()!=null)for(ResearchPage page:research.getPages()) {
            if(page==null || (page.type!=ResearchPage.PageType.TEXT && page.type!=ResearchPage.PageType.TEXT_CONCEALED))continue;
            if(page.type==ResearchPage.PageType.TEXT_CONCEALED && (page.research==null || !unlocked.test(page.research)))continue;
            String translated=page.getTranslatedText();
            if(translated==null || translated.equals(page.text) && translated.matches("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)+"))continue;
            text=plain(translated);if(!text.isEmpty())break;
        }
        if(text.isEmpty()) {
            String summary=research.getText();
            if(summary!=null && !summary.equals("tc.research_text."+research.key))text=plain(summary);
        }
        if(text.isEmpty())text=tr("mbo.research_scroll.unavailable");
        return new ResearchScrollContent(title,text,true,false,aspects);
    }
    // This is a text-only introduction: TC4 images and dividers stay in the Thaumonomicon.
    static String plain(String text) {
        if(text==null)return "";
        return text.replaceAll("(?is)<IMG>.*?</IMG>"," ")
                .replaceAll("(?i)<(?:BR|LINE)\\s*/?>","\n")
                .replaceAll("<[^>]*>","").trim();
    }
    private static String tr(String key){return StatCollector.translateToLocal(key);}
    private static String runes(){return "Arcana essentia cognitio\n\nOrdo permutatio potentia mysterium. Scientia incognita vinculum.\n\nLux umbra motus spiritus. Instrumentum praecantatio.";}
}
