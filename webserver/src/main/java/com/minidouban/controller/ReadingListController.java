package com.minidouban.controller;

import com.minidouban.component.CookieManager;
import com.minidouban.service.ReadingListBookService;
import com.minidouban.service.ReadingListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Controller
public class ReadingListController {
    @Resource
    private ReadingListService readingListService;

    @Resource
    private ReadingListBookService readingListBookService;
    @Resource
    private CookieManager cookieManager;

    @GetMapping ("/reading_list")
    public String readingList(Model model, String selectedListName, @CookieValue ("userId") Cookie userIdCookie) {
        Map<String, Long> readingListMap = readingListService.getListNameIdMap(cookieManager.getUserId(userIdCookie));
        model.addAttribute("readingListsName", readingListMap.keySet());
        if (!isEmpty(selectedListName)) {
            model.addAttribute("booksInList",
                               readingListBookService.getBooksInList(readingListMap.get(selectedListName)));
        }
        //TODO 为什么model变化了不刷新页面
        return "reading_list";
    }

    @PostMapping ("/rename-list")
    public String renameList(String oldListName, String desiredListName, @CookieValue ("userId") Cookie userIdCookie) {
        long userId = cookieManager.getUserId(userIdCookie);
        if (!isEmpty(oldListName) && !isEmpty(desiredListName)) {
            readingListService.renameReadingList(userId, oldListName, desiredListName);
        }
        return "redirect:/reading_list";
    }

    @PostMapping ("create-list")
    public String createList(String listNameToCreate, @CookieValue ("userId") Cookie userIdCookie) {
        if (!isEmpty(listNameToCreate)) {
            readingListService.createReadingList(cookieManager.getUserId(userIdCookie), listNameToCreate);
        }
        return "redirect:/reading_list";
    }

    @PostMapping ("/delete-list")
    public String deleteList(String listNameToDelete, @CookieValue ("userId") Cookie userIdCookie) {
        Map<String, Long> readingListMap = readingListService.getListNameIdMap(cookieManager.getUserId(userIdCookie));
        if (!isEmpty(listNameToDelete)) {
            readingListService.deleteReadingList(cookieManager.getUserId(userIdCookie),
                                                 readingListMap.get(listNameToDelete));
        }
        return "redirect:/reading_list";
    }

    @PostMapping ("/delete-all-list")
    public String deleteAllList(String ifDeleteAllList, @CookieValue ("userId") Cookie userIdCookie) {
        if ("1".equals(ifDeleteAllList)) {
            readingListService.deleteAllReadingLists(cookieManager.getUserId(userIdCookie));
        }
        return "redirect:/reading_list";
    }

    @PostMapping ("/remove-book")
    public String removeBook(RedirectAttributes redirectAttributes, String listNameDeleteFrom, String[] bookIdToRemove,
                             @CookieValue ("userId") Cookie userIdCookie) {
        if (isEmpty(listNameDeleteFrom)) {
            return "redirect:/reading_list";
        }
        long selectedListId = readingListService.getListNameIdMap(cookieManager.getUserId(userIdCookie))
                .get(listNameDeleteFrom);
        if (bookIdToRemove != null) {
            for (String bookId : bookIdToRemove) {
                readingListBookService.removeBookFromList(selectedListId, Long.parseLong(bookId));
            }
        }
        redirectAttributes.addAttribute("selectedListName", listNameDeleteFrom);
        return "redirect:/reading_list";
    }

    @PostMapping ("/add-book")
    public String addBookToList(String from, String[] bookIdToAdd, String selectedListName,
                                @CookieValue ("userId") Cookie userIdCookie) {
        Map<String, Long> readingListMap = readingListService.getListNameIdMap(cookieManager.getUserId(userIdCookie));
        long listId = readingListMap.get(selectedListName);
        for (String bookId : bookIdToAdd) {
            readingListBookService.addBookToList(listId, Long.parseLong(bookId));
        }
        switch (from) {
            case "2":
                return "redirect:/advanced_search";
            case "1":
            default:
                return "redirect:/search";
        }
    }
}
