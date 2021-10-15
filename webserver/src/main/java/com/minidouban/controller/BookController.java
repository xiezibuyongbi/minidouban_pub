package com.minidouban.controller;

import com.minidouban.component.CookieManager;
import com.minidouban.configuration.Prompt;
import com.minidouban.pojo.Book;
import com.minidouban.pojo.BookPredicate;
import com.minidouban.pojo.Page;
import com.minidouban.pojo.PageInfo;
import com.minidouban.service.BookService;
import com.minidouban.service.ReadingListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Controller
public class BookController {
    private static final int PAGE_SIZE = 10;
    @Resource
    private BookService bookService;
    @Resource
    private ReadingListService readingListService;
    @Resource
    private CookieManager cookieManager;

    @GetMapping ("/search")
    public String search(Model model, HttpSession session, String keyword,
                         @RequestParam (value = "pageNum", required = false, defaultValue = "0") int pageNum,
                         @CookieValue ("username") Cookie usernameCookie, @CookieValue ("userId") Cookie userIdCookie) {
        String username = cookieManager.getUsername(usernameCookie);
        if (!isEmpty(username)) {
            model.addAttribute("username", username);
            Map<String, Long> readingListMap = readingListService.getListNameIdMap(
                    cookieManager.getUserId(userIdCookie));
            model.addAttribute("readingListsName", readingListMap.keySet());
        }
        if (isEmpty(keyword)) {
            return "search";
        }
        Page<Book> page = bookService.findByKeyword(keyword, PageInfo.of(pageNum, PAGE_SIZE));
        if (page.isEmpty()) {
            model.addAttribute("msg", Prompt.NO_SEARCH_RESULT_PROMPT);
            return "search";
        }
        model.addAttribute("books", page.getContent());
        return "search";
    }

    @GetMapping ("/advanced_search")
    public String advancedSearch(Model model, HttpSession session,
                                 @RequestParam (value = "pageNum", required = false, defaultValue = "0") int pageNum,
                                 @RequestParam ("bookPredicate") BookPredicate bookPredicate,
                                 @CookieValue ("username") Cookie usernameCookie,
                                 @CookieValue ("userId") Cookie userIdCookie) {
        String username = cookieManager.getUsername(usernameCookie);
        if (!isEmpty(username)) {
            model.addAttribute("username", username);
            Map<String, Long> readingListMap = readingListService.getListNameIdMap(
                    cookieManager.getUserId(userIdCookie));
            model.addAttribute("readingListsName", readingListMap.keySet());
        }
        if (bookPredicate != null) {
            Page<Book> bookSlice = bookService.findFuzzily(bookPredicate, PageInfo.of(pageNum, PAGE_SIZE));
            model.addAttribute("books", bookSlice.getContent());
        }
        return "advanced_search";
    }

    @PostMapping ("/advanced_search")
    public String advancedSearch(HttpSession session, BookPredicate bookPredicate) {
        session.setAttribute("bookPredicate", bookPredicate);
        return "redirect:/advanced_search";
    }

}
