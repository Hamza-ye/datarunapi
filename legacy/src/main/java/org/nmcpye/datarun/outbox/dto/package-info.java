package org.nmcpye.datarun.outbox.dto;


//public void convertObjectToMapExample(Student studentObj) {
//    // Option 1: Simple conversion using raw Map.class
//    Map<String, Object> map = objectMapper.convertValue(studentObj, Map.class);
//    System.out.println(map);
//
//    // Option 2: More robust, using TypeReference for a specific Map type
//    // This is useful for more complex or typed maps
//    Map<String, Object> typedMap = objectMapper.convertValue(studentObj, new TypeReference<Map<String, Object>>() {});
// //    objectMapper.convertValue(studentObj, JsonNode.class);
//    System.out.println(typedMap);
//
//    //---------------------------------
//    // Convert the Map back to a POJO
//    User anotherUser = mapper.convertValue(map, User.class);
//
//    System.out.println(anotherUser.getName()); // Output: Jack
//    System.out.println(anotherUser.isMale());  // Output: true
//
//}
