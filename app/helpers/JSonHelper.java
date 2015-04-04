package helpers;

import java.util.List;

import models.SuperUser;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSonHelper {

	/**
	 * Returns JSon of all superUsers that contains id and email as objectNodes.
	 * 
	 * @return ArrayNode
	 */
	public static ArrayNode allSuperUsers() {

		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		List<SuperUser> users = SuperUser.allSuperUsers();
		for (SuperUser c : users) {
			ObjectNode sUser = Json.newObject();
			sUser.put("id", c.id);
			sUser.put("email", c.email);
			arrayNode.add(sUser);
		}
		return arrayNode;
	}

}
