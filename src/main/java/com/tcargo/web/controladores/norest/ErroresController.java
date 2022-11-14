package com.tcargo.web.controladores.norest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ErroresController implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping("/error")
    public ModelAndView errorHandler(HttpServletResponse response) {
        ModelAndView mav = null;
        int status = response.getStatus();

        if (status == 403) {
            mav = new ModelAndView(new RedirectView("/dashboard"));
        }

        return mav;
    }

}
