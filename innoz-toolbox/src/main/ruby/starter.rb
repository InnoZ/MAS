class Starter
	
	DEFAULT = '/home/dhosse/workspace/MAS/bla/'
	
	def initialize (id, year, folder = DEFAULT)
		@id = id
		@year = year
		@folder = folder
	end

	def start
		Kernel.exec "java -cp /home/dhosse/workspace/MAS/innoz-toolbox/target/innoz-toolbox-0.1-SNAPSHOT.jar com.innoz.toolbox.run.Main #{id} #{year} #{folder}"
	end

	private
	attr_reader :id, :year, :folder

end

Starter.new(15001,2017).start