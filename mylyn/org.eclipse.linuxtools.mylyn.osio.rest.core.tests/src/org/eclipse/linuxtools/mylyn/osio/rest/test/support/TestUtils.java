/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.mylyn.osio.rest.test.support;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Area;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.AreaAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.AreaListResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.GenericLinks;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.GenericLinksForSpace;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IdentityRelationData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Iteration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IterationAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IterationListResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Label;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LabelAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LabelListResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceListMeta;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceOwnedBy;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceRelationships;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceSingleResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.UserAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.UserSingleResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.UsersResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeAttributes;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeField;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeFieldType;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeResponse;

public class TestUtils {
	
	@SuppressWarnings("deprecation")
	public static void initSpaces(OSIOTestRestRequestProvider requestProvider, TestData test) {

		GenericLinksForSpace myworkLinks = new GenericLinksForSpace("/api/spaces/SPACE-0001", "",
				"/api/spaces/SPACE-0001/workitemtypes", "/api/spaces/SPACE-0001/workitemlinktypes",
				"/api/spaces/SPACE-0001/collaborators", "", "", null);

		WorkItemTypeFieldType stateFieldType = new WorkItemTypeFieldType("enum", "string", "", new String[] {"new", "open", "in progress", "resolved", "closed"});

		WorkItemTypeField field1 = new WorkItemTypeField("System status", "Status", Boolean.TRUE, stateFieldType);
		Map<String, WorkItemTypeField> fieldMap = new HashMap<>();	
		fieldMap.put("system.state", field1);
		
		WorkItemLinkTypeAttributes linkAttributes = new WorkItemLinkTypeAttributes("Bug Blocker", "a bug blocker", 1, "2017/01/01", "2017/01/01", "blocks",
				"is blocked by", null);
		WorkItemLinkTypeData linkType = new WorkItemLinkTypeData("LINKTYPE-0001", "workitemlinktypes", linkAttributes, null, null);
		test.defaultWorkItemLinkTypeResponse = (new WorkItemLinkTypeResponse(new WorkItemLinkTypeData[] {linkType}));
		
		
		WorkItemTypeAttributes bugTypeAttributes = new WorkItemTypeAttributes("bug", "A bug", 1, "bug", "2017/01/01", "2017/01/01", fieldMap);
		WorkItemTypeData bugData = new WorkItemTypeData("WORKITEMTYPE-0001", "workitemtypes", bugTypeAttributes, null);
		WorkItemTypeAttributes featureTypeAttributes = new WorkItemTypeAttributes("feature", "A feature", 1, "feature", "2017/01/01", "2017/01/01", fieldMap);
		WorkItemTypeData featureData = new WorkItemTypeData("WORKITEMTYPE-0002", "workitemtypes", featureTypeAttributes, null);

		WorkItemTypeData defaultWorkItemData[] = {bugData, featureData};
		test.defaultWorkItemTypeResponse = (new WorkItemTypeResponse(defaultWorkItemData));
		
		UserAttributes user1Attributes = new UserAttributes("user", "user", new Date(2017, 01, 01), new Date(2017, 01, 01), "User", "//imageurl", "user", Boolean.TRUE, 
				"user@user.org", "userCo", "", "//users/user", "");
		User user1 = new User("USER-0001", "users", user1Attributes, null);
		UserAttributes user2Attributes = new UserAttributes("user2", "user2", new Date(2017, 01, 01), new Date(2017, 01, 01), "User2", "//imageurl", "user2", Boolean.TRUE, 
				"user2@user.org", "userCo", "", "//users/user2", "");
		User user2 = new User("USER-0002", "users", user2Attributes, null);
		
		UsersResponse userResponse1 = new UsersResponse(new User[] {user1, user2});
		
		AreaAttributes area1Attributes = new AreaAttributes("mywork", 1, "2017/01/01", "2017/01/01", "", "");
		Area area1 = new Area("AREA-0001", "areas", area1Attributes, null, null);
		AreaAttributes area2Attributes = new AreaAttributes("otherarea", 1, "2017/01/01", "2017/01/01", "", "");
		Area area2 = new Area("AREA-0002", "areas", area2Attributes, null, null);
		
		AreaListResponse areaResponse1 = new AreaListResponse(new Area[] {area1, area2}, null, null);
	
		IterationAttributes iteration1Attributes = new IterationAttributes("mywork", "mywork iteration", "2017/01/01", "2017/01/01", "", "", "", true, true, "", "");
		Iteration iteration1 = new Iteration("ITERATION-0001", "iterations", iteration1Attributes, null, null);
		
		IterationListResponse iterationResponse1 = new IterationListResponse(new Iteration[] {iteration1}, null, null);
	
		SpaceAttributes myworkAttributes = new SpaceAttributes("mywork", "My work space", 1,
				"", "");
		
		SpaceOwnedBy ownedBy = new SpaceOwnedBy(new IdentityRelationData("USER-0001", "users"), 
				new GenericLinks("https://api.openshift.io/api/users/user",
				"https://api.openshift.io/api/users/user", null));
		
		SpaceRelationships relationships = new SpaceRelationships(ownedBy, null, null, null, null, null, null, null, null);
		Space mywork = new Space("SPACE-0001", "spaces", myworkAttributes, relationships, myworkLinks);
		SpaceSingleResponse myworkResponse = new SpaceSingleResponse(mywork);
		
		LabelAttributes label1Attributes = new LabelAttributes("label1", "2017/01/01", "2017/01/01", "1", "", "", "");
		Label label1 = new Label("labels", "LABEL-0001",label1Attributes, null, null);
		LabelAttributes label2Attributes = new LabelAttributes("label2", "2017/01/01", "2017/01/01", "1", "", "", "");
		Label label2 = new Label("labels", "LABEL-0002",label2Attributes, null, null);
		
		LabelListResponse labelResponse1 = new LabelListResponse(new Label[] {label1, label2}, null, null);

		requestProvider.addGetRequest("/spaces/SPACE-0001/workitemlinktypes", test.defaultWorkItemLinkTypeResponse);
		requestProvider.addGetRequest("/spaces/SPACE-0001/workitemtypes", test.defaultWorkItemTypeResponse);
		requestProvider.addGetRequest("/spaces/SPACE-0001/collaborators", userResponse1);
		requestProvider.addGetRequest("/spaces/SPACE-0001", myworkResponse);
		requestProvider.addGetRequest("/namedspaces/user/mywork", mywork);
		requestProvider.addGetRequest("/spaces/SPACE-0001/areas", areaResponse1);
		requestProvider.addGetRequest("/spaces/SPACE-0001/iterations", iterationResponse1);
		requestProvider.addGetRequest("/spaces/SPACE-0001/labels", labelResponse1);
		requestProvider.addGetRequest("/users/user", new UserSingleResponse(user1));

		GenericLinksForSpace mywork2Links = new GenericLinksForSpace("/api/spaces/SPACE-0002", "",
				"/api/spaces/SPACE-0002/workitemtypes", "/api/spaces/SPACE-0002/workitemlinktypes",
				"/api/spaces/SPACE-0002/collaborators", "", "", null);
		
		UsersResponse userResponse2 = new UsersResponse(new User[] {user1});
		
		
		AreaAttributes area3Attributes = new AreaAttributes("mywork2", 1, "2017/01/01", "2017/01/01", "", "");
		Area area3 = new Area("AREA-0003", "areas", area3Attributes, null, null);
		AreaListResponse areaResponse2 = new AreaListResponse(new Area[] {area3}, null, null);
		
		IterationAttributes iteration2Attributes = new IterationAttributes("mywork2", "mywork2 iteration", "2017/01/01", "2017/01/01", "", "", "", true, true, "", "");
		Iteration iteration2 = new Iteration("ITERATION-0002", "iterations", iteration2Attributes, null, null);
		IterationAttributes iteration3Attributes = new IterationAttributes("otheriteration", "mywork2 other iteration", "2017/01/01", "2017/01/01", "", "", "", true, true, "", "");
		Iteration iteration3 = new Iteration("ITERATION-0003", "iterations", iteration3Attributes, null, null);
		
		IterationListResponse iterationResponse2 = new IterationListResponse(new Iteration[] {iteration2, iteration3}, null, null);
	
		SpaceAttributes mywork2Attributes = new SpaceAttributes("mywork2", "My work space2", 1,
				"", "");
		SpaceRelationships relationships2 = new SpaceRelationships(ownedBy, null, null, null, null, null, null, null, null);

		Space mywork2 = new Space("SPACE-0002", "spaces", mywork2Attributes, relationships2, mywork2Links);
		SpaceSingleResponse mywork2Response = new SpaceSingleResponse(mywork2);
		
		LabelAttributes label3Attributes = new LabelAttributes("label3", "2017/01/01", "2017/01/01", "1", "", "", "");
		Label label3 = new Label("labels", "LABEL-0003",label3Attributes, null, null);
		LabelAttributes label4Attributes = new LabelAttributes("label4", "2017/01/01", "2017/01/01", "1", "", "", "");
		Label label4 = new Label("labels", "LABEL-0004",label4Attributes, null, null);
		
		LabelListResponse labelResponse2 = new LabelListResponse(new Label[] {label3, label4}, null, null);

		
		requestProvider.addGetRequest("/spaces/SPACE-0002/workitemlinktypes", test.defaultWorkItemLinkTypeResponse);
		requestProvider.addGetRequest("/spaces/SPACE-0002/workitemtypes", test.defaultWorkItemTypeResponse);
		requestProvider.addGetRequest("/spaces/SPACE-0002/collaborators", userResponse2);
		requestProvider.addGetRequest("/spaces/SPACE-0002", mywork2Response);
		requestProvider.addGetRequest("/namedspaces/user/mywork2", mywork2);
		requestProvider.addGetRequest("/spaces/SPACE-0002/areas", areaResponse2);
		requestProvider.addGetRequest("/spaces/SPACE-0002/iterations", iterationResponse2);
		requestProvider.addGetRequest("/spaces/SPACE-0002/labels", labelResponse2);
		requestProvider.addGetRequest("/users/user2", new UserSingleResponse(user2));
		
		GenericLinksForSpace mywork3Links = new GenericLinksForSpace("/api/spaces/SPACE-0003", "",
				"/api/spaces/SPACE-0003/workitemtypes", "/api/spaces/SPACE-0003/workitemlinktypes",
				"/api/spaces/SPACE-0003/collaborators", "", "", null);
		
		UsersResponse userResponse3 = new UsersResponse(new User[] {user2});
		
		
		AreaAttributes area4Attributes = new AreaAttributes("mywork", 1, "2017/01/01", "2017/01/01", "", "");
		Area area4 = new Area("AREA-0004", "areas", area4Attributes, null, null);
		AreaListResponse areaResponse3 = new AreaListResponse(new Area[] {area4}, null, null);
		
		IterationAttributes iteration4Attributes = new IterationAttributes("mywork", "mywork b iteration", "2017/01/01", "2017/01/01", "", "", "", true, true, "", "");
		Iteration iteration4 = new Iteration("ITERATION-0004", "iterations", iteration4Attributes, null, null);
		
		IterationListResponse iterationResponse3 = new IterationListResponse(new Iteration[] {iteration4}, null, null);
	
		SpaceAttributes mywork3Attributes = new SpaceAttributes("mywork", "My work space3", 1,
				"", "");
		SpaceOwnedBy ownedBy3= new SpaceOwnedBy(new IdentityRelationData("USER-0002", "users"), 
				new GenericLinks("https://api.openshift.io/api/users/user2",
				"https://api.openshift.io/api/users/user2", null));
		
		SpaceRelationships relationships3 = new SpaceRelationships(ownedBy3, null, null, null, null, null, null, null, null);

		Space mywork3 = new Space("SPACE-0003", "spaces", mywork3Attributes, relationships3, mywork3Links);
		SpaceSingleResponse mywork3Response = new SpaceSingleResponse(mywork3);
		
		LabelAttributes label5Attributes = new LabelAttributes("label5", "2017/01/01", "2017/01/01", "1", "", "", "");
		Label label5 = new Label("labels", "LABEL-0005",label5Attributes, null, null);
		
		LabelListResponse labelResponse3 = new LabelListResponse(new Label[] {label5}, null, null);

		
		requestProvider.addGetRequest("/spaces/SPACE-0003/workitemlinktypes", test.defaultWorkItemLinkTypeResponse);
		requestProvider.addGetRequest("/spaces/SPACE-0003/workitemtypes", test.defaultWorkItemTypeResponse);
		requestProvider.addGetRequest("/spaces/SPACE-0003/collaborators", userResponse3);
		requestProvider.addGetRequest("/spaces/SPACE-0003", mywork3Response);
		requestProvider.addGetRequest("/namedspaces/user2/mywork", mywork3);
		requestProvider.addGetRequest("/spaces/SPACE-0003/areas", areaResponse3);
		requestProvider.addGetRequest("/spaces/SPACE-0003/iterations", iterationResponse3);
		requestProvider.addGetRequest("/spaces/SPACE-0003/labels", labelResponse3);


		Map<String, Space> spaceMap = new TreeMap<>();
		spaceMap.put("mywork", mywork);
		spaceMap.put("mywork2", mywork2);
		test.spaceMap = spaceMap;
		
		SpaceResponse spaces = new SpaceResponse(new Space[] {mywork, mywork2}, null, new SpaceListMeta(2));
		test.spaces = spaces;
		SpaceResponse externalspaces = new SpaceResponse(new Space[] {mywork3}, null, new SpaceListMeta(1));
		test.externalspaces = externalspaces;
		
		requestProvider.addGetRequest("/namedspaces/user", spaces);

	}


}
