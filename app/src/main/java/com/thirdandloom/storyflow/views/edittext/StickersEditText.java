package com.thirdandloom.storyflow.views.edittext;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.thirdandloom.storyflow.Theme;

import com.thirdandloom.storyflow.utils.StickersUtils;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action2;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StickersEditText extends EmojiconEditText {
    private static final String FIND_REG_EXP = "\\[(\\w*?)\\]|\\[(\\w*?)*-(\\w*?)\\]";
    private static final String FIND_DELETED_REG_EXP = "\\[\\s*(\\w+)*$|\\[\\s*(\\w+)*\\s|\\[\\s*(\\w+)*[\\[]";

    public static final Pattern FIND_IMAGE_PATTERN = Pattern.compile(FIND_REG_EXP);
    private static final Pattern FIND_DELETE_IMAGE_PATTERN = Pattern.compile(FIND_DELETED_REG_EXP);
    private static final char START_STICKER = "[".charAt(0);
    private static final char END_STICKER = "]".charAt(0);

    private List<StickersUtils.DisplayedSticker> oldDetectedStickers;

    public StickersEditText(Context context) {
        this(context, null);
    }

    public StickersEditText(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public StickersEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<StickersUtils.DisplayedSticker> getOldDetectedStickers() {
        if (oldDetectedStickers == null) {
            oldDetectedStickers = new ArrayList<>();
        }
        return oldDetectedStickers;
    }

    @Override
    protected void onSelectionChanged(final int selStart, final int selEnd) {
        detectGroupStickerSelection(getText(), selStart, selEnd, getOldDetectedStickers(),
                this::setSelection, () -> {
                    detectTapStickerSelection(selStart, selEnd, getOldDetectedStickers(),
                            this::setSelection, () -> {
                                super.onSelectionChanged(selStart, selEnd);
                            });
                });
    }

    private static void detectTapStickerSelection(int selStart, int selEnd, List<StickersUtils.DisplayedSticker> oldDetectedStickers,
            Action2<Integer, Integer> detected, Action0 notDetected) {
        if (selStart == selEnd) {
            StickersUtils.DisplayedSticker sticker = getDisplayedStickerForPosition(selStart, oldDetectedStickers);
            if (sticker != null) {
                selStart = selStart == sticker.startPosition
                        ? selStart
                        : sticker.endPosition;
                selEnd = selStart;
                detected.call(selStart, selEnd);
                return;
            }
        }
        notDetected.call();
    }

    private static void detectGroupStickerSelection(Editable text, int selStart, int selEnd, List<StickersUtils.DisplayedSticker> oldDetectedStickers,
            Action2<Integer, Integer> detected, Action0 notDetected) {
        if (selStart != selEnd) {
            boolean found = false;
            String currentText = text.toString();
            String selectedText = currentText.substring(selStart, selEnd);
            boolean lastSymbolIsStartSticker = selectedText.charAt(selectedText.length() - 1) == START_STICKER;
            boolean firstSymbolIsEndSticker = selectedText.charAt(0) == END_STICKER;

            if (lastSymbolIsStartSticker) {
                String detectedSticker = getStickerWithStartStickerPos(selEnd - 1, oldDetectedStickers);
                if (detectedSticker != null) {
                    found = true;
                    selEnd = selEnd + detectedSticker.length() - 1;
                }
            }
            if (firstSymbolIsEndSticker) {
                String detectedSticker = getStickerWithEndStickerPos(selStart + 1, oldDetectedStickers);
                if (detectedSticker != null) {
                    found = true;
                    selStart = selStart + 1;
                }
            }
            if (found) {
                detected.call(selStart, selEnd);
                return;
            }
        }
        notDetected.call();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        int oldTextLength = getText().length();
        int oldSelectionEnd = getSelectionEnd();
        getTextWithImages(getOldDetectedStickers(), getText(), getSelectionStart(), getSelectionEnd(),
                (newText, detectedStickers) -> {
                    oldDetectedStickers = detectedStickers;
                    setText(newText);
                    setSelection(oldSelectionEnd - (oldTextLength - newText.length()));
                }, detectedStickers -> {
                    oldDetectedStickers = detectedStickers;
                    super.onTextChanged(text, start, lengthBefore, lengthAfter);
                });
    }

    private static void getTextWithImages(List<StickersUtils.DisplayedSticker> oldDetectedStickers, final Editable text, int selectionStart, int selectionEnd,
                                          Action2<Editable, List<StickersUtils.DisplayedSticker>> onChanged, Action1<List<StickersUtils.DisplayedSticker>> onNotChanged) {
        Matcher matcher = FIND_IMAGE_PATTERN.matcher(text.toString());
        StickersUtils.removeImageSpans(text);
        List<StickersUtils.DisplayedSticker> detectedStickers = StickersUtils.addNewSpans(matcher, text);

        checkRemovedStickers(text, oldDetectedStickers, detectedStickers, newEditable -> {
            onChanged.call(newEditable, detectedStickers);
        }, () -> {
            checkLastSticker(text, selectionEnd, selectionStart, oldDetectedStickers, newEditable -> {
                onChanged.call(newEditable, detectedStickers);
            }, () -> {
                onNotChanged.call(detectedStickers);
            });
        });
    }

    private static void checkLastSticker(Editable text, int selectionEnd, int selectionStart, List<StickersUtils.DisplayedSticker> oldDetectedStickers,
            Action1<Editable> detected, Action0 notDetected) {
        if (selectionEnd == selectionStart && selectionEnd > 2) {
            int lastStickerPosition = selectionEnd + 1;
            String sticker = getStickerWithEndStickerPos(lastStickerPosition, oldDetectedStickers);
            if (sticker != null) {
                StringBuilder stringBuilder = new StringBuilder(text);
                stringBuilder.replace(selectionEnd - (sticker.length() - 1), selectionEnd, "");
                detected.call(new SpannableStringBuilder(stringBuilder));
                return;
            }
        }
        notDetected.call();
    }

    private static void checkRemovedStickers(Editable text, List<StickersUtils.DisplayedSticker> oldDetectedStickers, List<StickersUtils.DisplayedSticker> detectedStickers,
            Action1<Editable> detected, Action0 notDetected) {
        boolean found = false;
        if (oldDetectedStickers.size() > detectedStickers.size()) {
            Matcher deletedMatcher = FIND_DELETE_IMAGE_PATTERN.matcher(text.toString());
            while (deletedMatcher.find()) {
                String sticker = deletedMatcher.group();
                boolean isPart = isPart(sticker);
                if (isPart) {
                    found = true;
                    StringBuilder stringBuilder = new StringBuilder(text);
                    if (sticker.charAt(sticker.length() - 1) == START_STICKER) {
                        stringBuilder.replace(deletedMatcher.start() + 1, deletedMatcher.end(), "");
                    } else {
                        stringBuilder.replace(deletedMatcher.start(), deletedMatcher.end(), "");
                    }
                    text = new SpannableStringBuilder(stringBuilder);
                }
            }
        }
        if (found) {
            detected.call(text);
        } else {
            notDetected.call();
        }
    }

    /**
     *
     * @param endPosition
     * @return null if not detected
     */
    @Nullable
    private static String getStickerWithEndStickerPos(int endPosition, List<StickersUtils.DisplayedSticker> oldDetectedStickers) {
        for (StickersUtils.DisplayedSticker sticker : oldDetectedStickers) {
            if (sticker.endPosition == endPosition) {
                return sticker.text;
            }
        }
        return null;
    }

    /**
     *
     * @param startPosition
     * @return null if not detected
     */
    private static String getStickerWithStartStickerPos(int startPosition, List<StickersUtils.DisplayedSticker> oldDetectedStickers) {
        for (StickersUtils.DisplayedSticker sticker : oldDetectedStickers) {
            if (sticker.startPosition == startPosition) {
                return sticker.text;
            }
        }
        return null;
    }

    @Nullable
    private static StickersUtils.DisplayedSticker getDisplayedStickerForPosition(int position, List<StickersUtils.DisplayedSticker> detectedStickers) {
        for (StickersUtils.DisplayedSticker sticker :  detectedStickers) {
            if (sticker.startPosition <= position
                    && sticker.endPosition >= position) {
                return sticker;
            }
        }
        return null;
    }

    private static boolean isPart(String sticker) {
        if (sticker.charAt(sticker.length() - 1) == START_STICKER) {
            sticker = sticker.substring(0, sticker.length() - 2);
        }
        for (String currentSticker : Theme.Stickers.catMapKeysList) {
            if (currentSticker.contains(sticker)) {
                return true;
            }
        }
        return false;
    }
}
