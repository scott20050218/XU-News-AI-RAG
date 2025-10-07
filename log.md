1,First I use AI help me to create a prototype, Because this prototype is Frontend, I did not know how to describe the UI, I used some function requirement to write. So AI get me ,the work is not perfect. Then I got him some pictures, AI can get me the results according to the picture.
PS: Think deeply, ask clearly , concisely

2. For todo-backend project, It is very hard for me to check the function of the AIProcessingService. I already told AI to get me a "AI 智能代理深度加工服务类", that meaning's AI smart agent deep processing service class. But I check the code, the class is very simple, just the String Comparison. The same situation happened in langchain project. So I ask AI, I need an advance , useful, smart function, why did you write the easy code? AI answered me, he think it is easy for and it can meet the requirement.
   PS: Learn technology, learn businesses, check the code, check the results

3. For langchain project, I created a scaffolding and asked AI to help me to add data collecting, data vectorization. I requested Ai to get data through todo-backend api restful , but I gave AI the api-interface.md , this experience was taught by our teacher. And the data could be saved in vector database, because todo-backend used summery to express the content, otherwise, langchain get the content from data.content.
   PS: More practice, more experience, define the interface clearly

4. For IDE Cursor , it is different from IDEA, especially when you are using the java project. IDEA can give you JAVA verify information, but Cursor can not.

5. For communication between two system, pay attention to the inferface, the Boundary. just like the pageunm, Is it from 0 or 1?

6. For the timezone, ISO 8601
   "2025-09-27T21:09:39.747314"，during the function change 2025-09-28
   export function parseTime(time, pattern) {
   if (arguments.length === 0 || !time) {
   return null
   }
   const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
   let date
   if (typeof time === 'object') {
   date = time
   } else {
   if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
   time = parseInt(time)
   } else if (typeof time === 'string') {
   time = time.replace(new RegExp(/-/gm), '/').replace('T', ' ').replace(new RegExp(/\.[\d]{3}/gm), '');
   }
   if ((typeof time === 'number') && (time.toString().length === 10)) {
   time = time \* 1000
   }
   date = new Date(time)
   }

   7.For the IDE

   8.For the Cursor
   power tools
   token limit

   9.For development

   - learn technology, in order to solve the problem, to understand through the code, to check what is the function, why to write
   - learn business, in order to control the business logic
   - summarize and record AI development the step, method, caution
   - suggest: there are some template for the AI development, such as the code template, spring boot ,spring cloud, vuejs, react , nodejs python , the business template, all kinds of documents , the summary template, report , project management etc. And these templates have been approved by the company, so we can use them directly.
