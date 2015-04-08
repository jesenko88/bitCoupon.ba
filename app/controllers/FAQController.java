package controllers;

import helpers.AdminFilter;
import models.FAQ;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.Logger;
import views.html.admin.faq.*;

public class FAQController extends Controller {

	/**
	 * Show FAQ page with list of all Frequently Asked Questions If current user
	 * is admin, an edit and delete button are showed for each question.
	 * 
	 * @return
	 */
	public static Result showFAQ() {
		return ok(FAQview.render(session("name"), FAQ.all()));
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
			DynamicForm form = Form.form().bindFromRequest();

			if (form.hasErrors() || form.hasGlobalErrors()) {
				Logger.debug("error in Add FAQ form");
				flash("error", " Error! "); // TODO message
				return ok((NewFAQ.render(session("name"))));
			}

			String question = form.data().get("question");
			String answer = form.data().get("answer");

			if (question.length() < 20 || answer.length() < 20) {
				Logger.debug(session("name")
						+ " entered a too short question/answer");
				flash("error",
						"Please, fill out both fields with valid a form! "
								+ "Each field should contain at least 20 characters.");
				return ok((NewFAQ.render(session("name"))));
			}

			FAQ.createFAQ(question, answer);
			Logger.debug(session("name") + " added a new FAQ");
			flash("success", "New Question added");
			return ok(NewFAQ.render(session("name")));
		} catch (Exception e) {
			flash("error", "Error while adding new FAQ. Please check you logs");
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
		return ok(EditFAQ.render(session("name"), question));
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
				flash("error", " Form has errors! ");
				return ok((EditFAQ.render(session("name"), FAQToUpdate)));
			}

			String question = form.data().get("question");
			String answer = form.data().get("answer");

			if (question.length() < 20 || answer.length() < 20) {
				Logger.debug(session("name")
						+ " entered a too short question/answer in 'updateFAQ' ");
				flash("error",
						"Each field should contain at least 20 characters.");
				return ok((EditFAQ.render(session("name"), FAQToUpdate)));
			}

			FAQToUpdate.question = question;
			FAQToUpdate.answer = answer;
			FAQ.update(FAQToUpdate);

			Logger.info(session("name") + " updated FAQ: " + id);
			flash("success", " Update Successful! ");
			return ok(EditFAQ.render(session("name"), FAQToUpdate));
		} catch (Exception e) {
			flash("error",
					"Error occured while updating FAQ. Please check your logs.");
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
			flash("success", "Question deleted!");
			return ok(FAQview.render(session("name"), FAQ.all()));
		} catch (Exception e) {
			flash("error",
					"Error occured while deleting FAQ. Please check your logs");
			Logger.error("Error at deleteFAQ: " + e.getMessage());
			return redirect("/");
		}
	}
}
