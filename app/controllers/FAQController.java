package controllers;

import helpers.AdminFilter;
import models.FAQ;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.faq.EditFAQ;
import views.html.admin.faq.FAQview;
import views.html.admin.faq.NewFAQ;
import api.JSonHelper;

public class FAQController extends Controller {
	
	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");

	/**
	 * Show FAQ page with list of all Frequently Asked Questions If current user
	 * is admin, an edit and delete button are showed for each question.
	 * 
	 * @return
	 */
	public static Result showFAQ() {
		if (request().accepts("html/text")) {
			return ok(FAQview.render(session("name"), FAQ.all()));
		}
		return ok(JSonHelper.faqListToJSon(FAQ.all()));
	}

	/**
	 * Show 'Add new FAQ' page
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result addFAQView() {
		return ok(NewFAQ.render(session("name")));
	}

	/**
	 * Add new FAQ to the database
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result addFAQ() {
		try {
			DynamicForm newFAQForm = Form.form().bindFromRequest();

			if (newFAQForm.hasErrors() || newFAQForm.hasGlobalErrors()) {
				Logger.debug("error in Add FAQ form");
				flash("error", Messages.get("error.msg.01"));
				return ok(NewFAQ.render(session("name")));
			}

			String question = newFAQForm.data().get("question");
			String answer = newFAQForm.data().get("answer");

			if (question.length() < 20 || answer.length() < 20) {
				Logger.debug(session("name") + " entered a too short question/answer");
				flash("error", Messages.get("faq.shortLength"));
				return ok((NewFAQ.render(session("name"))));
			}

			FAQ.createFAQ(question, answer);
			Logger.debug(session("name") + " added a new FAQ");
			flash("success", Messages.get("faq.QuestineAdded"));
			return ok(NewFAQ.render(session("name")));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at addFAQ: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Show Edit FAQ page
	 * 
	 * @param id
	 *            of the question
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result editFAQView(int id) {
		FAQ question = FAQ.find(id);
		String name = session("name");
		if (name == null || question == null) {
			flash("error", Messages.get("error.msg.01"));
			return redirect("/");
		}
		return ok(EditFAQ.render(name, question));
	}

	/**
	 * Edit existing FAQ and update it in the database
	 * 
	 * @param id
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result updateFAQ(int id) {
		try {
			DynamicForm form = Form.form().bindFromRequest();
			FAQ FAQToUpdate = FAQ.find(id);
			if (form.hasErrors() || form.hasGlobalErrors()) {
				Logger.debug("error in edit FAQ form");
				flash("error", Messages.get("error.form"));
				return ok((EditFAQ.render(session("name"), FAQToUpdate)));
			}
			String question = form.data().get("question");
			String answer = form.data().get("answer");
			if (question.length() < 20 || answer.length() < 20) {
				Logger.debug(session("name")+ " entered a too short question/answer in 'updateFAQ' ");
				flash("error", Messages.get("faq.shortLength"));
				return ok((EditFAQ.render(session("name"), FAQToUpdate)));
			}
			FAQToUpdate.question = question;
			FAQToUpdate.answer = answer;
			FAQ.update(FAQToUpdate);

			Logger.info(session("name") + " updated FAQ: " + id);
			flash("success", Messages.get("updateSuccess"));
			return ok(EditFAQ.render(session("name"), FAQToUpdate));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at updateFAQ" + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Delete existing FAQ
	 * 
	 * @param id
	 *            of the question
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deleteFAQ(int id) {
		try {
			FAQ.delete(id);
			Logger.info(session("name") + " deleted FAQ: " + id);
			flash("success", Messages.get("delete.success"));
			return ok(FAQview.render(session("name"), FAQ.all()));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at deleteFAQ: " + e.getMessage());
			return redirect("/");
		}
	}
}
